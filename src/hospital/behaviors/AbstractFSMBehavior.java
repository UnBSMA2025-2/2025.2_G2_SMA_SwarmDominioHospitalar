package hospital.behaviors;

import hospital.agents.PersonAgent;
import hospital.agents.ChildAgent;
import hospital.agents.AdultAgent;
import hospital.agents.ElderAgent;
import hospital.enums.Local;
import hospital.model.Bairro;
import hospital.model.Doenca;
import jade.core.behaviours.TickerBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AbstractFSMBehavior<T extends PersonAgent> extends TickerBehaviour {

    protected final Bairro bairro;
    protected final Random rand = new Random();
    protected int diasCompletos = 0;
    protected final int LIMITE_DIAS = 3;
    protected int tickDoDia = 0;

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
            System.out.println("[" + agente.getLocalName() + "] ganhou casa em (" + posCasa[0] + "," + posCasa[1] + ")");
        }

        // ===================== ROTINA DIÁRIA =====================
        Local localAtual = definirLocalDoDia(agente, tickDoDia);

        // Atualiza posição
        if (localAtual == Local.CASA) {
            agente.setPos(agente.getHomeX(), agente.getHomeY());
        } else {
            int[] pos = encontrarPosicaoLocal(localAtual, agente);
            agente.setPos(pos[0], pos[1]);
        }

        System.out.println("[" + agente.getLocalName() + "] está em: " + localAtual);

        // ===================== CHECA INFECÇÃO =====================
        checarInfeccaoGenerica(agente, bairro.getTodosAgentesNoLocal(agente.getPosX(), agente.getPosY()));

        // ===================== PRÓXIMO TICK =====================
        tickDoDia++;
        if (tickDoDia > 2) {
            tickDoDia = 0;
            diasCompletos++;
            if (diasCompletos >= LIMITE_DIAS) {
                System.out.println("Fim dos dias! [" + agente.getLocalName() + "] finalizou simulação.");
                myAgent.doDelete();
            }
        }
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
        double C_loc = 1.0;
        double S_i = 1.0;
        double pNaoInfectado = 1.0;

        for (Object outro : agentesNoLocal) {
            boolean infectado = false;
            Doenca d = null;

            if (outro instanceof ChildAgent c) {
                infectado = c.isInfectado();
                d = c.getDoenca();
            } else if (outro instanceof AdultAgent a) {
                infectado = a.isInfectado();
                d = a.getDoenca();
            } else if (outro instanceof ElderAgent e) {
                infectado = e.isInfectado();
                d = e.getDoenca();
            }

            if (infectado && d != null) {
                double pTrans = d.getBeta() * C_loc * S_i * d.getInfectividade();
                String nome = (outro instanceof ChildAgent c2) ? c2.getLocalName()
                        : (outro instanceof AdultAgent a2) ? a2.getLocalName()
                        : ((ElderAgent) outro).getLocalName();
                System.out.println("    Transmissão de " + nome + ": pTrans=" + pTrans);
                pNaoInfectado *= (1 - pTrans);
            }
        }

        double pInfeccao = 1 - pNaoInfectado;
        if (rand.nextDouble() < pInfeccao) {
            if (agente instanceof ChildAgent c) c.setInfectado(true);
            else if (agente instanceof AdultAgent a) a.setInfectado(true);
            else if (agente instanceof ElderAgent e) e.setInfectado(true);

            System.out.println("    " + agente.getLocalName() + " foi infectado!");
        }
    }
}