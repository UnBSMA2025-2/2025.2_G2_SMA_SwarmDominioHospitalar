package hospital.agents;

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
    protected Doenca doenca;
    protected double vulnerabilidade; // 0.0 (resistente) → 1.0 (muito vulnerável)
    private GravidadeSintoma sintomaAtual = GravidadeSintoma.NENHUM;
    protected final Random rand = new Random();
    private int ticksDesdeInfeccao = 0;
    private int diasDesdeInfeccao = -1;   // -1 significa que não está infectado
    private int ticksDesdeUltimaMudanca = 0; // controle de tempo entre pioras

    // ===================== ENUM DE SINTOMAS =====================
    public enum GravidadeSintoma {
        NENHUM,
        LEVE,
        MODERADO,
        GRAVE,
        MORTE
    }

    // ===================== POSIÇÃO =====================
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

    public double getVulnerabilidade() { return vulnerabilidade; }
    public void setVulnerabilidade(double vulnerabilidade) { this.vulnerabilidade = vulnerabilidade; }

    protected void configurarVulnerabilidade() {
        this.vulnerabilidade = 1.0; // padrão neutro
    }

    public Doenca getDoenca() { return doenca; }

    public void infectar(Doenca doenca) {
        this.infectado = true;
        this.doenca = doenca;
        this.ticksDesdeInfeccao = 0;
        this.diasDesdeInfeccao = 0;
        this.ticksDesdeUltimaMudanca = 0;
    }

    // ===================== GETTERS E SETTERS DE SINTOMA =====================
    public GravidadeSintoma getSintomaAtual() {
        return sintomaAtual;
    }

    public void setSintomaAtual(GravidadeSintoma sintoma) {
        this.sintomaAtual = sintoma;
    }

    // ============================================================
    //  PROGRESSÃO PROBABILÍSTICA DE SINTOMAS (MAIS LENTA)
    // ============================================================

    public void avancarInfeccao() {
        if (!infectado || sintomaAtual == GravidadeSintoma.MORTE) return;

        ticksDesdeInfeccao++;
        ticksDesdeUltimaMudanca++;

        // Define um intervalo mínimo entre pioras (para dar tempo de infectar)
        int intervaloMinimo = 3 + rand.nextInt(3); // entre 3 e 5 ticks
        if (ticksDesdeUltimaMudanca < intervaloMinimo) return;

        double chancePiorar = 0.0;
        double chanceMelhorar = 0.0;

        // Reduz a agressividade das chances
        switch (sintomaAtual) {
            case NENHUM -> {
                chancePiorar = vulnerabilidade * 0.25; // antes era 0.5
            }
            case LEVE -> {
                chancePiorar = vulnerabilidade * 0.4; // antes era 0.7
                chanceMelhorar = (1 - vulnerabilidade) * 0.1;
            }
            case MODERADO -> {
                chancePiorar = vulnerabilidade * 0.55; // antes era 0.85
                chanceMelhorar = (1 - vulnerabilidade) * 0.15;
            }
            case GRAVE -> {
                chancePiorar = vulnerabilidade * 0.65; // antes era 0.95
                chanceMelhorar = (1 - vulnerabilidade) * 0.2;
            }
            default -> { return; }
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
            case GRAVE -> sintomaAtual = GravidadeSintoma.MORTE;
            case MORTE -> {}
        }
        System.out.println(getLocalName() + " piorou para " + sintomaAtual + " ⚠️");
    }

    private void melhorarSintoma() {
        switch (sintomaAtual) {
            case GRAVE -> sintomaAtual = GravidadeSintoma.MODERADO;
            case MODERADO -> sintomaAtual = GravidadeSintoma.LEVE;
            case LEVE -> sintomaAtual = GravidadeSintoma.NENHUM;
            default -> {}
        }
        System.out.println(getLocalName() + " melhorou para " + sintomaAtual + " ✅");
    }

    // ===================== SETUP COMUM =====================
    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.bairro = (Bairro) args[0];
        }

        this.doenca = criarDoenca();
        configurarVulnerabilidade();

        System.out.println(getEmoji() + " " + getLocalName() +
                " criado(a)! Vulnerabilidade: " + String.format("%.2f", vulnerabilidade));

        // Registra o agente no controlador de sincronização
        ACLMessage reg = new ACLMessage(ACLMessage.INFORM);
        reg.setConversationId("REGISTER_AGENT");
        reg.addReceiver(new AID("syncController", AID.ISLOCALNAME));
        send(reg);

        adicionarAoBairro();
        addBehaviour(criarBehaviour());
    }

    // ===================== SINCRONIZAÇÃO =====================
    public void waitForNextTick() {
        MessageTemplate mt = MessageTemplate.MatchConversationId("TICK_GO");
        ACLMessage msg = blockingReceive(mt);
        if (msg != null) {
            int nextTick = Integer.parseInt(msg.getContent());
            System.out.println("⏩ " + getLocalName() + " iniciado tick " + nextTick);
        }
    }

    public void notifyTickDone(int currentTick) {
        ACLMessage done = new ACLMessage(ACLMessage.INFORM);
        done.setConversationId("TICK_DONE");
        done.setContent(String.valueOf(currentTick));
        done.addReceiver(new AID("syncController", AID.ISLOCALNAME));
        send(done);
    }

    // ===================== MÉTODOS ABSTRATOS =====================
    protected abstract TickerBehaviour criarBehaviour();
    protected abstract void adicionarAoBairro();
    protected abstract Doenca criarDoenca();
    protected abstract String getEmoji();

    @Override
    protected void takeDown() {
        System.out.println("😴 " + getLocalName() + " encerrou suas atividades.");
    }
}
