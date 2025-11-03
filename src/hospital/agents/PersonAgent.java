package hospital.agents;

import hospital.behaviors.AbstractFSMBehavior;
import hospital.logging.LoggerSMA;
import hospital.model.Bairro;
import hospital.model.Doenca;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Random;

public abstract class PersonAgent extends Agent {

    protected Bairro bairro;
    protected int posX = 0;
    protected int posY = 0;
    protected int homeX;
    protected int homeY;
    protected boolean casaDefinida = false;

    protected boolean infectado = false;
    protected boolean curado = false; // 🩹 novo atributo
    protected Doenca doenca;
    protected double vulnerabilidade;

    private GravidadeSintoma sintomaAtual = GravidadeSintoma.NENHUM;
    protected final Random rand = new Random();
    private int ticksDesdeInfeccao = 0;
    private int diasDesdeInfeccao = -1;
    private int ticksDesdeUltimaMudanca = 0;

    private AbstractFSMBehavior<?> behavior;

    public enum GravidadeSintoma {
        NENHUM, LEVE, MODERADO, GRAVE, MORTE
    }

    // ---------------- Getters / Setters ----------------
    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public void setPos(int x, int y) { this.posX = x; this.posY = y; }

    public int getHomeX() { return homeX; }
    public int getHomeY() { return homeY; }
    public void setCasa(int x, int y) { this.homeX = x; this.homeY = y; }

    public boolean isCasaDefinida() { return casaDefinida; }
    public void setCasaDefinida(boolean casaDefinida) { this.casaDefinida = casaDefinida; }

    public boolean isInfectado() { return infectado; }
    public void setInfectado(boolean infectado) { this.infectado = infectado; }

    public boolean isCurado() { return curado; }
    public void setCurado(boolean curado) { this.curado = curado; }

    public Doenca getDoenca() { return doenca; }
    public double getVulnerabilidade() { return vulnerabilidade; }
    public void setVulnerabilidade(double vulnerabilidade) { this.vulnerabilidade = vulnerabilidade; }

    public GravidadeSintoma getSintomaAtual() { return sintomaAtual; }
    public void setSintomaAtual(GravidadeSintoma sintoma) { this.sintomaAtual = sintoma; }

    // ---------------- Configuração inicial ----------------
    protected void configurarVulnerabilidade() {
        this.vulnerabilidade = 0.5 + rand.nextDouble() * 0.5;
    }

    public void infectar(Doenca doenca) {
        this.infectado = true;
        this.curado = false;
        this.doenca = doenca;
        this.ticksDesdeInfeccao = 0;
        this.diasDesdeInfeccao = 0;
        this.ticksDesdeUltimaMudanca = 0;

        LoggerSMA.event(this, "💉 %s foi infectado com %s.", getLocalName(), doenca.getNome());
    }

    public void setBehavior(AbstractFSMBehavior<?> behavior) { this.behavior = behavior; }
    public AbstractFSMBehavior<?> getBehavior() { return behavior; }

    // ---------------- Progressão da infecção ----------------
    public void avancarInfeccao() {
        if (!infectado || sintomaAtual == GravidadeSintoma.MORTE) return;

        ticksDesdeInfeccao++;
        ticksDesdeUltimaMudanca++;

        int intervaloMinimo = 2 + rand.nextInt(2);
        if (ticksDesdeUltimaMudanca < intervaloMinimo) return;

        double chancePiorar = 0.0;
        double chanceMelhorar = 0.0;

        switch (sintomaAtual) {
            case NENHUM -> chancePiorar = vulnerabilidade * 0.35;
            case LEVE -> {
                chancePiorar = vulnerabilidade * 0.55;
                chanceMelhorar = (1 - vulnerabilidade) * 0.05;
            }
            case MODERADO -> {
                chancePiorar = vulnerabilidade * 0.75;
                chanceMelhorar = (1 - vulnerabilidade) * 0.08;
            }
            case GRAVE -> {
                chancePiorar = vulnerabilidade * 0.9;
                chanceMelhorar = (1 - vulnerabilidade) * 0.1;
            }
        }

        double variacao = (rand.nextDouble() * 0.1) - 0.05;
        chancePiorar = Math.min(1.0, Math.max(0.0, chancePiorar + variacao));

        double sorte = rand.nextDouble();
        if (sorte < chancePiorar) {
            piorarSintoma();
            ticksDesdeUltimaMudanca = 0;
        } else if (sorte < chancePiorar + chanceMelhorar) {
            melhorarSintoma();
            ticksDesdeUltimaMudanca = 0;
        }
    }

    private void piorarSintoma() {
        switch (sintomaAtual) {
            case NENHUM -> sintomaAtual = GravidadeSintoma.LEVE;
            case LEVE -> sintomaAtual = GravidadeSintoma.MODERADO;
            case MODERADO -> sintomaAtual = GravidadeSintoma.GRAVE;
            case GRAVE -> {
                sintomaAtual = GravidadeSintoma.MORTE;
                LoggerSMA.warn(this, "☠️ %s faleceu (piorou para MORTE).", getLocalName());
                infectado = false;
                curado = false;

                // 🧩 Notifica o SyncController da morte antes de ser removido
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setConversationId("MORTE_AGENT");
                msg.addReceiver(new AID("syncController", AID.ISLOCALNAME));
                send(msg);

                if (bairro != null) bairro.marcarComoMorto(this);
                doDelete();
                return;
            }
        }
        LoggerSMA.warn(this, "⚠️ %s piorou para %s.", getLocalName(), sintomaAtual);
    }

    private void melhorarSintoma() {
        switch (sintomaAtual) {
            case GRAVE -> sintomaAtual = GravidadeSintoma.MODERADO;
            case MODERADO -> sintomaAtual = GravidadeSintoma.LEVE;
            case LEVE -> {
                sintomaAtual = GravidadeSintoma.NENHUM;
                infectado = false;
                curado = true;
                LoggerSMA.info(this, "💚 %s se curou completamente!", getLocalName());
                return;
            }
        }
        LoggerSMA.info(this, "💚 %s melhorou para %s.", getLocalName(), sintomaAtual);
    }

    // ---------------- Setup ----------------
    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) this.bairro = (Bairro) args[0];

        this.doenca = criarDoenca();
        configurarVulnerabilidade();

        LoggerSMA.system("%s %s criado(a) | Vulnerabilidade: %.2f",
                getEmoji(), getLocalName(), vulnerabilidade);

        ACLMessage reg = new ACLMessage(ACLMessage.INFORM);
        reg.setConversationId("REGISTER_AGENT");
        reg.addReceiver(new AID("syncController", AID.ISLOCALNAME));
        send(reg);

        adicionarAoBairro();

        TickerBehaviour fsm = criarBehaviour();
        if (fsm instanceof AbstractFSMBehavior<?> fsmTyped) setBehavior(fsmTyped);
        addBehaviour(fsm);

        // Responde à consulta de vulnerabilidade (Hospital usa isso)
        addBehaviour(new jade.core.behaviours.CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchConversationId("CONSULTA_VULNERABILIDADE"));
                if (msg != null) {
                    ACLMessage reply = msg.createReply();
                    reply.setConversationId("RESPOSTA_VULNERABILIDADE");
                    reply.setContent(String.valueOf(getVulnerabilidade()));
                    send(reply);
                } else block();
            }
        });
    }

    // ---------------- Sincronização de ticks ----------------
    public void waitForNextTick() {
        MessageTemplate mt = MessageTemplate.MatchConversationId("TICK_GO");
        ACLMessage msg = blockingReceive(mt);
        if (msg != null) {
            int nextTick = Integer.parseInt(msg.getContent());
            LoggerSMA.event(this, "⏩ Tick %d iniciado para %s.", nextTick, getLocalName());
        }
    }

    public void notifyTickDone(int currentTick) {
        ACLMessage done = new ACLMessage(ACLMessage.INFORM);
        done.setConversationId("TICK_DONE");
        done.setContent(String.valueOf(currentTick));
        done.addReceiver(new AID("syncController", AID.ISLOCALNAME));
        send(done);
    }

    // ---------------- Métodos abstratos ----------------
    protected abstract TickerBehaviour criarBehaviour();
    protected abstract void adicionarAoBairro();
    protected abstract Doenca criarDoenca();
    protected abstract String getEmoji();

    @Override
    protected void takeDown() {
        LoggerSMA.event(this, "😴 %s finalizado.", getLocalName());
    }
}
