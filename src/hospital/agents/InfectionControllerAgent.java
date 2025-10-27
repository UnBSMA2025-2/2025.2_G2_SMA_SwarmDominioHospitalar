package hospital.agents;

import hospital.behaviors.AbstractFSMBehavior;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.util.List;

public class InfectionControllerAgent extends Agent {

    private final long tickPeriod = 1100; // 1 segundo, ou ajustável

    @Override
    protected void setup() {
        System.out.println("🦠 " + getLocalName() + " iniciado. Controlando infecções...");

        addBehaviour(new TickerBehaviour(this, tickPeriod) {
            @Override
            protected void onTick() {
                List<PersonAgent> aInfectar = AbstractFSMBehavior.getAInfectarNoTick();

                synchronized (aInfectar) {
                    if (!aInfectar.isEmpty()) {
                        System.out.println("\n=== [Controlador] Processando infecções do tick ===");
                        for (PersonAgent p : aInfectar) {
                            if (!p.isInfectado()) {
                                p.setInfectado(true);
                                System.out.println("💉 " + p.getLocalName() + " foi infectado.");
                            }
                        }
                        aInfectar.clear();
                    }
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println("🛑 " + getLocalName() + " finalizado.");
    }
}