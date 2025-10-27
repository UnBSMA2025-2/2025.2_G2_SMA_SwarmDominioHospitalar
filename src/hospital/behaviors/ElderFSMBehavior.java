package hospital.behaviors;

import hospital.agents.ElderAgent;
import hospital.agents.ChildAgent;
import hospital.enums.Local;
import hospital.model.Doenca;
import hospital.model.Bairro;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ElderFSMBehavior extends TickerBehaviour {

    private int diasCompletos = 0;
    private final int LIMITE_DIAS = 3;
    private int tickDoDia = 0; // 0=manhã, 1=tarde, 2=noite
    private final Bairro bairro;
    private final Random rand = new Random();

    public ElderFSMBehavior(Agent a, long period, Bairro bairro) {
        super(a, period);
        this.bairro = bairro;
    }

    @Override
    protected void onTick() {
        ElderAgent agente = (ElderAgent) myAgent;

        // ===================== ATRIBUI CASA FIXA (apenas 1x) =====================
        if (!agente.isCasaDefinida()) {
            int[] posCasa = encontrarCasaDisponivel();
            agente.setCasa(posCasa[0], posCasa[1]);
            agente.setPos(posCasa[0], posCasa[1]);
            agente.setCasaDefinida(true);
            System.out.println("[" + agente.getLocalName() + "] ganhou casa em (" + posCasa[0] + "," + posCasa[1] + ")");
        }

        // ===================== ROTINA DIÁRIA =====================
        Local localAtual = switch (tickDoDia) {
            case 0 -> Local.TRABALHO;
            case 1 -> {
                int escolha = rand.nextInt(3);
                yield switch (escolha) {
                    case 0 -> Local.FESTA;
                    case 1 -> Local.PARQUE;
                    default -> Local.CASA;
                };
            }
            case 2 -> Local.CASA;
            default -> Local.CASA;
        };

        // Atualiza a posição do agente
        if (localAtual == Local.CASA) {
            agente.setPos(agente.getHomeX(), agente.getHomeY());
        } else {
            int[] pos = encontrarPosicaoLocal(localAtual, agente);
            agente.setPos(pos[0], pos[1]);
        }

        System.out.println("[" + agente.getLocalName() + "] está em: " + localAtual);

        // Calcula infecção
        checarInfeccao(agente);

        // Próximo tick
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

    // ===================== ESCOLHE UMA CASA DISPONÍVEL =====================
    private int[] encontrarCasaDisponivel() {
        List<int[]> casasLivres = new ArrayList<>();
        for (int i = 0; i < bairro.getLinhas(); i++) {
            for (int j = 0; j < bairro.getColunas(); j++) {
                if (bairro.getLocal(i, j) == Local.CASA) {
                    boolean ocupada = !bairro.getAgentesNoLocalElder(i, j).isEmpty();
                    if (!ocupada) casasLivres.add(new int[]{i, j});
                }
            }
        }
        if (casasLivres.isEmpty()) return new int[]{0, 0};
        return casasLivres.get(rand.nextInt(casasLivres.size()));
    }

    // ===================== INFECÇÃO =====================
    private void checarInfeccao(ElderAgent agente) {
        if (agente.isInfectado()) return;

        List<Object> agentesNoLocal = bairro.getTodosAgentesNoLocal(agente.getPosX(), agente.getPosY());
        double pNaoInfectado = 1.0;
        double C_loc = 1.0;
        double S_i = 1.0;

        for (Object outro : agentesNoLocal) {
            boolean infectado = false;
            Doenca d = null;

            if (outro instanceof ChildAgent c) {
                infectado = c.isInfectado();
                d = c.getDoenca();
            } else if (outro instanceof ElderAgent a) {
                infectado = a.isInfectado();
                d = a.getDoenca();
            }

            if (infectado && d != null) {
                double I_j = d.getInfectividade();
                double pTrans = d.getBeta() * C_loc * S_i * I_j;
                String nome = (outro instanceof ChildAgent c2) ? c2.getLocalName() : ((ElderAgent) outro).getLocalName();
                System.out.println("    Transmissão de " + nome + ": pTrans=" + pTrans);
                pNaoInfectado *= (1 - pTrans);
            }
        }

        double pInfeccao = 1 - pNaoInfectado;
        if (rand.nextDouble() < pInfeccao) {
            agente.setInfectado(true);
            System.out.println("    " + agente.getLocalName() + " foi infectado!");
        }
    }

    // ===================== ENCONTRAR POSICAO =====================
    private int[] encontrarPosicaoLocal(Local local, ElderAgent agente) {
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

        if (posicoes.isEmpty()) {
            return new int[]{agente.getPosX(), agente.getPosY()};
        }

        return posicoes.get(rand.nextInt(posicoes.size()));
    }
}