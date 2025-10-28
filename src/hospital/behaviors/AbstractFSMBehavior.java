package hospital.behaviors;

import hospital.agents.PersonAgent;
import hospital.enums.Local;
import hospital.model.Bairro;
import jade.core.behaviours.TickerBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AbstractFSMBehavior<T extends PersonAgent> extends TickerBehaviour {

    protected final Bairro bairro;
    protected final Random rand = new Random();
    protected int diasCompletos = 0;
    protected final int LIMITE_DIAS = 5;
    protected int tickDoDia = 0;
    private static List<PersonAgent> aInfectarNoTick = new ArrayList<>(); // controla o tick global

    public AbstractFSMBehavior(T agente, long period, Bairro bairro) {
        super(agente, period);
        this.bairro = bairro;
    }

    @Override
    protected void onTick() {
        T agente = (T) myAgent;

        // ===================== ATRIBUI CASA FIXA =====================
        if (!agente.isCasaDefinida()) {
            int[] posCasa = encontrarCasaDisponivel(agente);
            agente.setCasa(posCasa[0], posCasa[1]);
            agente.setPos(posCasa[0], posCasa[1]);
            agente.setCasaDefinida(true);
        }

        // ===================== ROTINA DIÁRIA =====================
        Local localAtual = definirLocalDoDia(agente, tickDoDia);

        if (localAtual == Local.CASA) {
            agente.setPos(agente.getHomeX(), agente.getHomeY());
        } else {
            int[] pos = encontrarPosicaoLocal(localAtual, agente);
            agente.setPos(pos[0], pos[1]);
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
                return;
            }
        }

        // ===================== SINCRONIZAÇÃO GLOBAL DE TICK =====================
        // 1. Notifica o controlador que este agente terminou o tick atual
        agente.notifyTickDone(tickDoDia);

        // 2. Espera até receber liberação do SyncControllerAgent para continuar
        agente.waitForNextTick();
    }

    // ===================== MÉTODOS ABSTRATOS =====================
    protected abstract Local definirLocalDoDia(T agente, int tickDoDia);
    protected abstract int[] encontrarCasaDisponivel(T agente);

    // ===================== MÉTODO GENÉRICO PARA POSIÇÃO =====================
    protected int[] encontrarPosicaoLocal(Local local, T agente) {
        List<int[]> posicoes = new ArrayList<>();

        for (int i = 0; i < bairro.getLinhas(); i++) {
            for (int j = 0; j < bairro.getColunas(); j++) {
                if (bairro.getLocal(i, j) == local) {
                    if (!(i == agente.getPosX() && j == agente.getPosY())) {
                        posicoes.add(new int[]{i, j});
                    }
                }
            }
        }

        if (posicoes.isEmpty()) return new int[]{agente.getPosX(), agente.getPosY()};
        return posicoes.get(rand.nextInt(posicoes.size()));
    }

    // ===================== MÉTODO GENÉRICO DE INFECÇÃO =====================
    protected void checarInfeccaoGenerica(T agente, List<Object> agentesNoLocal) {
        List<PersonAgent> paraInfectar = new ArrayList<>();

        for (Object outro : agentesNoLocal) {
            if (outro instanceof PersonAgent p) {
                if (p.isInfectado() && p.getDoenca() != null && !agente.isInfectado()) {
                    double pTrans = p.getDoenca().getBeta() * p.getDoenca().getInfectividade();
                    if (rand.nextDouble() < pTrans) {
                        paraInfectar.add(agente); // marca para infectar
                    }
                }
            }
        }

        // Atualiza a lista global de forma sincronizada
        if (!paraInfectar.isEmpty()) {
            synchronized (aInfectarNoTick) {
                aInfectarNoTick.addAll(paraInfectar);
            }
        }
    }

    public static List<PersonAgent> getAInfectarNoTick() {
        return aInfectarNoTick;
    }
}
