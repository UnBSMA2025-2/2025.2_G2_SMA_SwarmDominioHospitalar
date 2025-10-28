package hospital.agents;

import hospital.model.Bairro;
import hospital.model.Doenca;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class PersonAgent extends Agent {

    protected Bairro bairro;
    protected int posX = 0;
    protected int posY = 0;
    protected int homeX;
    protected int homeY;
    protected boolean casaDefinida = false;
    protected boolean infectado = false;
    protected Doenca doenca;

    private int ticksDesdeInfeccao = 0;
    private int diasDesdeInfeccao = -1;   // -1 significa que n esta infectado

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

    public void setInfectado(boolean infectado) {
        this.infectado = infectado;
    }

    public Doenca getDoenca() { return doenca; }

    public void infectar(Doenca doenca){
        this.infectado = true;
        this.doenca = doenca;
        this.ticksDesdeInfeccao = 0;
        this.diasDesdeInfeccao = 0;
    }

    public void avancarInfeccao(){
        if(isInfectado()){
            ticksDesdeInfeccao++;
            if(ticksDesdeInfeccao >= 3){
                diasDesdeInfeccao++;
                ticksDesdeInfeccao = 0;
            }
        }
    }

    public enum GravidadeSintoma{
        NENHUM,
        LEVE,
        MODERADO,
        GRAVE,
        MORTE
    }

    public GravidadeSintoma getSintomaAtual(){
        if(!isInfectado()){
            return GravidadeSintoma.NENHUM;
        }
        return switch (diasDesdeInfeccao) {
            case 1 -> GravidadeSintoma.LEVE;
            case 2 -> GravidadeSintoma.MODERADO;
            case 3 -> GravidadeSintoma.GRAVE;
            default -> {
                if (diasDesdeInfeccao >= 4) {
                    yield GravidadeSintoma.MORTE;
                }
                yield GravidadeSintoma.NENHUM;
            }
        };
    }

    // ===================== SETUP COMUM =====================
    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.bairro = (Bairro) args[0];
        }

        // Configura doença padrão (subclasses podem alterar)
        this.doenca = criarDoenca();

        System.out.println(getEmoji() + " " + getLocalName() + " criado(a)!");

        // Registra o agente no controlador de sincronização (SyncControllerAgent)
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

    // ===================== MÉTODOS ABSTRATOS PARA SUBCLASSES =====================
    protected abstract TickerBehaviour criarBehaviour();
    protected abstract void adicionarAoBairro();
    protected abstract Doenca criarDoenca();
    protected abstract String getEmoji();

    @Override
    protected void takeDown() {
        System.out.println("😴 " + getLocalName() + " encerrou suas atividades.");
    }

    // ===================== HOSPITALIZAÇÃO =====================
    private boolean hospitalizado = false;
    
    public boolean isHospitalizado() {
        return hospitalizado;
    }
    
    public void setHospitalizado(boolean hospitalizado) {
        this.hospitalizado = hospitalizado;
    }
}




