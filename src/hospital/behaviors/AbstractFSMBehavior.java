package hospital.behaviors;

import hospital.agents.PersonAgent;
import hospital.enums.Local;
import hospital.model.Bairro;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AbstractFSMBehavior<T extends PersonAgent> extends TickerBehaviour {

    protected final Bairro bairro;
    protected final Random rand = new Random();

    protected int diasCompletos = 0;
    protected final int LIMITE_DIAS = 999;
    protected int tickDoDia = 0;

    private static final List<PersonAgent> aInfectarNoTick = new ArrayList<>();

    // Controle hospitalar
    private boolean tentouHospital = false;
    private boolean internado = false;
    private int ticksNoHospital = 0;
    private final AID hospitalAID = new AID("hospital1", AID.ISLOCALNAME);

    public AbstractFSMBehavior(T agente, long period, Bairro bairro) {
        super(agente, period);
        this.bairro = bairro;
    }

    @Override
    protected void onTick() {
        T agente = (T) myAgent;

        // ===================== CASA FIXA =====================
        if (!agente.isCasaDefinida()) {
            int[] posCasa = encontrarCasaDisponivel(agente);
            agente.setCasa(posCasa[0], posCasa[1]);
            agente.setPos(posCasa[0], posCasa[1]);
            agente.setCasaDefinida(true);
        }

        // ===================== AVANÇA INFECÇÃO =====================
        agente.avancarInfeccao();

        // ===================== CHECA HOSPITAL =====================
        boolean indoHospital = agente.isInfectado() && deveProcurarHospital(agente);

        if (indoHospital) {
            // Teleporta para hospital
            int[] posHospital = bairro.getHospitalPos();
            agente.setPos(posHospital[0], posHospital[1]);

            // Solicita internação se ainda não tentou
            if (!tentouHospital && !internado) {
                solicitarInternacao(agente);
                tentouHospital = true;
            }

            // Chance de melhora se internado
            if (internado) {
                ticksNoHospital++;
                double chanceMelhora = Math.min(0.9, ticksNoHospital * 0.05);
                if (rand.nextDouble() < chanceMelhora) {
                    agente.setInfectado(false);
                    agente.setSintomaAtual(PersonAgent.GravidadeSintoma.NENHUM);
                    internado = false;
                    System.out.println("💚 " + agente.getLocalName() + " se recuperou no hospital!");
                }
            }

            // Recebe alta médica
            MessageTemplate mtAlta = MessageTemplate.MatchConversationId("ALTA_MEDICA");
            ACLMessage msgAlta = myAgent.receive(mtAlta);
            if (msgAlta != null && "CURADO".equals(msgAlta.getContent())) {
                agente.setInfectado(false);
                agente.setSintomaAtual(PersonAgent.GravidadeSintoma.NENHUM);
                internado = false;
                System.out.println("💚 " + agente.getLocalName() + " recebeu alta médica!");
            }

        } else {
            // ===================== ROTINA DIÁRIA =====================
            Local localAtual = definirLocalDoDia(agente, tickDoDia);

            if (localAtual == Local.CASA) {
                agente.setPos(agente.getHomeX(), agente.getHomeY());
            } else {
                int[] pos = encontrarPosicaoLocal(localAtual, agente);
                agente.setPos(pos[0], pos[1]);
            }
        }

        // ===================== CHECA INFECÇÃO =====================
        checarInfeccaoGenerica(agente, bairro.getTodosAgentesNoLocal(agente.getPosX(), agente.getPosY()));

        // ===================== PRÓXIMO TICK =====================
        tickDoDia++;
        if (tickDoDia > 2) {
            tickDoDia = 0;
            diasCompletos++;
            if (diasCompletos >= LIMITE_DIAS) {
                myAgent.doDelete();
            }
        }
    }

    protected boolean deveProcurarHospital(T agente) {
        PersonAgent.GravidadeSintoma sintoma = agente.getSintomaAtual();
        double v = agente.getVulnerabilidade();

        // Mortos não procuram hospital
        if (sintoma == PersonAgent.GravidadeSintoma.MORTE) return false;

        // Vulnerabilidade alta: vai se estiver pelo menos moderado
        if (v > 0.5) {
            return sintoma == PersonAgent.GravidadeSintoma.MODERADO
                    || sintoma == PersonAgent.GravidadeSintoma.GRAVE;
        }

        // Vulnerabilidade baixa: só vai se estiver grave
        return sintoma == PersonAgent.GravidadeSintoma.GRAVE;
    }

    // ============== COMUNICAÇÃO COM O HOSPITAL ==================


    private void solicitarInternacao(T agente) {
        // Evita envio de mensagens por agentes mortos
        if (agente.getSintomaAtual() == PersonAgent.GravidadeSintoma.MORTE) {
            System.out.println("⚰️ Pedido de internação ignorado: " + agente.getLocalName() + " já faleceu.");
            return;
        }

        ACLMessage pedido = new ACLMessage(ACLMessage.REQUEST);
        pedido.setConversationId("PEDIDO_HOSPITAL");
        pedido.setContent("PRECISO_DE_TRATAMENTO");
        pedido.addReceiver(hospitalAID);
        myAgent.send(pedido);

        System.out.println("🏥 " + agente.getLocalName() + " solicitou internação em " + hospitalAID.getLocalName());

        MessageTemplate mt = MessageTemplate.MatchConversationId("PEDIDO_HOSPITAL");
        ACLMessage resposta = myAgent.receive(mt);
        if (resposta != null) {
            if ("ADMITIDO".equals(resposta.getContent())) {
                internado = true;
                System.out.println("✅ " + agente.getLocalName() + " foi internado com sucesso!");
            } else if ("LOTADO".equals(resposta.getContent())) {
                System.out.println("🚫 " + hospitalAID.getLocalName() + " está lotado! " + agente.getLocalName() + " não conseguiu vaga.");
            }
        }
    }

    // ====================== INFECÇÃO =============================

    protected void checarInfeccaoGenerica(T agente, List<Object> agentesNoLocal) {
        List<PersonAgent> paraInfectar = new ArrayList<>();

        for (Object outro : agentesNoLocal) {
            if (outro instanceof PersonAgent p) {
                // Mortos não infectam
                if (p.getSintomaAtual() == PersonAgent.GravidadeSintoma.MORTE) continue;

                if (p.isInfectado() && p.getDoenca() != null && !agente.isInfectado()) {
                    double pTrans = p.getDoenca().getBeta() * p.getDoenca().getInfectividade();
                    if (rand.nextDouble() < pTrans) {
                        paraInfectar.add(agente);
                    }
                }
            }
        }

        if (!paraInfectar.isEmpty()) {
            synchronized (aInfectarNoTick) {
                aInfectarNoTick.addAll(paraInfectar);
            }
        }
    }


    // ====================== AUXILIARES ===========================


    protected abstract Local definirLocalDoDia(T agente, int tickDoDia);
    protected abstract int[] encontrarCasaDisponivel(T agente);

    protected int[] encontrarPosicaoLocal(Local local, T agente) {
        List<int[]> posicoes = new ArrayList<>();

        for (int i = 0; i < bairro.getLinhas(); i++) {
            for (int j = 0; j < bairro.getColunas(); j++) {
                if (bairro.getLocal(i, j) == local) {
                    posicoes.add(new int[]{i, j});
                }
            }
        }

        if (posicoes.isEmpty()) return new int[]{agente.getPosX(), agente.getPosY()};
        return posicoes.get(rand.nextInt(posicoes.size()));
    }

    public static List<PersonAgent> getAInfectarNoTick() {
        return aInfectarNoTick;
    }
}
