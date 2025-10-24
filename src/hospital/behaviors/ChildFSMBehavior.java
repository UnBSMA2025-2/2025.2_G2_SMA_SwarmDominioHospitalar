package hospital.behaviors;

import hospital.agents.ChildAgent;
import hospital.enums.Local;
import hospital.model.Doenca;
import hospital.model.Bairro;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChildFSMBehavior extends TickerBehaviour {

    private int diasCompletos = 0;
    private final int LIMITE_DIAS = 3;
    private int tickDoDia = 0; // 0=manhã, 1=tarde, 2=noite
    private final Bairro bairro;
    private final Random rand = new Random();

    public ChildFSMBehavior(Agent a, long period, Bairro bairro) {
        super(a, period);
        this.bairro = bairro;
    }

    @Override
    protected void onTick() {
        ChildAgent agente = (ChildAgent) myAgent;

        Local proximoLocal = null;
        switch (tickDoDia) {
            case 0 -> proximoLocal = Local.ESCOLA;
            case 1 -> proximoLocal = rand.nextBoolean() ? Local.PARQUE : Local.ATIVIDADE;
            case 2 -> proximoLocal = Local.CASA;
        }

        // Escolher posição aleatória dentro do local
        int[] pos = encontrarPosicaoLocal(proximoLocal);
        agente.setPos(pos[0], pos[1]);

        // Calcular infecção após mover
        checarInfeccao(agente, proximoLocal);

        // Próximo tick
        tickDoDia++;
        if (tickDoDia > 2) {
            tickDoDia = 0;
            diasCompletos++;
            if (diasCompletos >= LIMITE_DIAS) {
                System.out.println("Fim dos dias! [" + agente.getLocalName() + "] vai dormir para sempre.");
                myAgent.doDelete();
            }
        }
    }

    private int[] encontrarPosicaoLocal(Local local) {
        List<int[]> possiveis = new ArrayList<>();
        for (int i = 0; i < bairro.getLinhas(); i++) {
            for (int j = 0; j < bairro.getColunas(); j++) {
                if (bairro.getLocal(i, j) == local) {
                    possiveis.add(new int[]{i, j});
                }
            }
        }
        if (possiveis.isEmpty()) return new int[]{0, 0};
        return possiveis.get(rand.nextInt(possiveis.size())); // posição aleatória
    }

    private void checarInfeccao(ChildAgent agente, Local local) {
        if (agente.isInfectado()) return; // já está infectado

        List<ChildAgent> agentesNaCelula = bairro.getAgentesNoLocal(agente.getPosX(), agente.getPosY());

        double pNaoInfectado = 1.0;
        double C_loc = 1.0; // fator do local
        double S_i = 1.0;   // suscetibilidade do agente

        for (ChildAgent outro : agentesNaCelula) {
            if (outro.isInfectado()) {
                Doenca d = outro.getDoenca();
                double I_j = d.getInfectividade();
                double pTrans = d.getBeta() * C_loc * S_i * I_j;
                System.out.println("    Transmissão de " + outro.getLocalName() + ": Beta=" + d.getBeta() +
                        ", Infectividade=" + I_j + ", pTrans=" + pTrans);
                pNaoInfectado *= (1 - pTrans);
            }
        }

        double pInfeccao = 1 - pNaoInfectado;
        double sorteio = rand.nextDouble(); // valor aleatório

        if (sorteio < pInfeccao) {
            agente.setInfectado(true);
        }
    }
}