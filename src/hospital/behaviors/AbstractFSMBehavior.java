package hospital.behaviors;

import hospital.agents.PersonAgent;
import hospital.enums.Local;
import hospital.logging.LoggerSMA;
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

    // Controle de vida/morte
    private boolean morto = false;

    public AbstractFSMBehavior(T agente, long period, Bairro bairro) {
        super(agente, period);
        this.bairro = bairro;
    }

    @Override
    protected void onTick() {
        T agente = (T) myAgent;

        // ===================== CHECA SE ESTÁ MORTO =====================
        if (morto || agente.getSintomaAtual() == PersonAgent.GravidadeSintoma.MORTE) {
            if (!morto) {
                morto = true;
                LoggerSMA.error(agente, "💀 %s faleceu e será removido da simulação.", agente.getLocalName());
            }
            myAgent.doDelete(); // encerra o agente JADE
            return;
        }

        // ===================== CASA FIXA =====================
        if (!agente.isCasaDefinida()) {
            int[] posCasa = encontrarCasaDisponivel(agente);
            agente.setCasa(posCasa[0], posCasa[1]);
            agente.setPos(posCasa[0], posCasa[1]);
            agente.setCasaDefinida(true);
            LoggerSMA.info(agente, "🏠 %s definiu casa em (%d,%d).", agente.getLocalName(), posCasa[0], posCasa[1]);
        }

        // ===================== AVANÇA INFECÇÃO =====================
        agente.avancarInfeccao();

        // ===================== CHECA HOSPITAL =====================
        boolean indoHospital = agente.isInfectado() && deveProcurarHospital(agente);

        if (indoHospital) {
            int[] posHospital = bairro.getHospitalPos();
            agente.setPos(posHospital[0], posHospital[1]);
            LoggerSMA.event(agente, "🚑 %s movendo-se para o hospital em (%d,%d).",
                    agente.getLocalName(), posHospital[0], posHospital[1]);

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
                    LoggerSMA.info(agente, "💚 %s se recuperou no hospital!", agente.getLocalName());
                }
            }

            // Recebe alta médica
            MessageTemplate mtAlta = MessageTemplate.MatchConversationId("ALTA_MEDICA");
            ACLMessage msgAlta = myAgent.receive(mtAlta);
            if (msgAlta != null && "CURADO".equals(msgAlta.getContent())) {
                agente.setInfectado(false);
                agente.setSintomaAtual(PersonAgent.GravidadeSintoma.NENHUM);
                internado = false;
                LoggerSMA.event(agente, "💚 %s recebeu alta médica!", agente.getLocalName());
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
            //LoggerSMA.info(agente, "🚶 %s moveu-se para %s (%d,%d).",
            //        agente.getLocalName(), localAtual, agente.getPosX(), agente.getPosY());
        }

        // ===================== CHECA INFECÇÃO =====================
        checarInfeccaoGenerica(agente, bairro.getTodosAgentesNoLocal(agente.getPosX(), agente.getPosY()));

        // ===================== PRÓXIMO TICK =====================
        tickDoDia++;
        if (tickDoDia > 2) {
            tickDoDia = 0;
            diasCompletos++;
            if (diasCompletos >= LIMITE_DIAS) {
                LoggerSMA.warn(agente, "🕰️ %s completou o limite de dias (%d) e será encerrado.", agente.getLocalName(), LIMITE_DIAS);
                myAgent.doDelete();
            }
        }
    }

    protected boolean deveProcurarHospital(T agente) {
        PersonAgent.GravidadeSintoma sintoma = agente.getSintomaAtual();
        double v = agente.getVulnerabilidade();

        // Mortos não procuram hospital
        if (sintoma == PersonAgent.GravidadeSintoma.MORTE || morto) return false;

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
        if (morto || agente.getSintomaAtual() == PersonAgent.GravidadeSintoma.MORTE) {
            LoggerSMA.warn(agente, "⚰️ Pedido de internação ignorado: %s já faleceu.", agente.getLocalName());
            return;
        }

        ACLMessage pedido = new ACLMessage(ACLMessage.REQUEST);
        pedido.setConversationId("PEDIDO_HOSPITAL");
        pedido.setContent("PRECISO_DE_TRATAMENTO");
        pedido.addReceiver(hospitalAID);
        myAgent.send(pedido);

        LoggerSMA.event(agente, "🏥 %s solicitou internação em %s.", agente.getLocalName(), hospitalAID.getLocalName());

        MessageTemplate mt = MessageTemplate.MatchConversationId("PEDIDO_HOSPITAL");
        ACLMessage resposta = myAgent.receive(mt);
        if (resposta != null) {
            if ("ADMITIDO".equals(resposta.getContent())) {
                internado = true;
                LoggerSMA.info(agente, "✅ %s foi internado com sucesso!", agente.getLocalName());
            } else if ("LOTADO".equals(resposta.getContent())) {
                LoggerSMA.warn(agente, "🚫 %s está lotado! %s não conseguiu vaga.",
                        hospitalAID.getLocalName(), agente.getLocalName());
            }
        }
    }

    // ====================== INFECÇÃO =============================

    protected void checarInfeccaoGenerica(T agente, List<Object> agentesNoLocal) {
        if (morto || agente.getSintomaAtual() == PersonAgent.GravidadeSintoma.MORTE) return;

        List<PersonAgent> paraInfectar = new ArrayList<>();

        for (Object outro : agentesNoLocal) {
            if (outro instanceof PersonAgent p) {
                if (p.getSintomaAtual() == PersonAgent.GravidadeSintoma.MORTE) continue;

                if (p.isInfectado() && p.getDoenca() != null && !agente.isInfectado()) {
                    double pTrans = p.getDoenca().getBeta() * p.getDoenca().getInfectividade();
                    if (rand.nextDouble() < pTrans) {
                        paraInfectar.add(agente);
                        LoggerSMA.event(agente, "💉 %s foi exposto à infecção por %s (β=%.2f).",
                                agente.getLocalName(), p.getLocalName(), pTrans);
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
                if (bairro.getLocal(i, j) == local) posicoes.add(new int[]{i, j});
            }
        }
        if (posicoes.isEmpty()) return new int[]{agente.getPosX(), agente.getPosY()};
        return posicoes.get(rand.nextInt(posicoes.size()));
    }

    public static List<PersonAgent> getAInfectarNoTick() {
        return aInfectarNoTick;
    }

    // ====================== VIDA / MORTE ===========================

    public boolean isMorto() { return morto; }
    public void setMorto(boolean morto) { this.morto = morto; }
}
