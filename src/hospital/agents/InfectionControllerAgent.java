package hospital.agents;

import hospital.behaviors.AbstractFSMBehavior;
import hospital.model.Bairro;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.util.List;

public class InfectionControllerAgent extends Agent {

    private final long tickPeriod = 1100;
    private int tickAtual = 0;
    private Bairro bairro;

    @Override
    protected void setup() {
        System.out.println("🦠 " + getLocalName() + " iniciado. Controlando infecções...");

        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof Bairro b) this.bairro = b;
        else {
            System.out.println("⚠️ Nenhum bairro recebido! Criando novo temporário (debug).");
            this.bairro = new Bairro();
        }

        addBehaviour(new TickerBehaviour(this, tickPeriod) {
            @Override
            protected void onTick() {
                List<PersonAgent> aInfectar = AbstractFSMBehavior.getAInfectarNoTick();

                synchronized (aInfectar) {
                    if (!aInfectar.isEmpty()) {
                        System.out.println("\n=== [Controlador] Processando infecções do tick ===" + tickAtual);
                        for (PersonAgent p : aInfectar) {
                            if (!p.isInfectado()) {
                                p.infectar(p.getDoenca());
                                System.out.println("💉 " + p.getLocalName() + " foi infectado no" + tickAtual);
                            }
                        }
                        aInfectar.clear();
                    }
                }

                if (bairro != null) bairro.removerAgentesMortos();
                tickAtual++;
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println("🛑 " + getLocalName() + " finalizado.");
    }
}
