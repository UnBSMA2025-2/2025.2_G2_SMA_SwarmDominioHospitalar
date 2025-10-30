package hospital.agents;

import hospital.behaviors.AbstractFSMBehavior;
import hospital.logging.LoggerSMA;
import hospital.model.Bairro;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.util.List;

public class InfectionControllerAgent extends Agent {

    private final long tickPeriod = 1100; // período de atualização
    private int tickAtual = 0;
    private Bairro bairro;

    @Override
    protected void setup() {
        LoggerSMA.system("🦠 %s iniciado. Controlando infecções...", getLocalName());

        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof Bairro b) {
            this.bairro = b;
        } else {
            LoggerSMA.warn(this, "⚠️ Nenhum bairro recebido! Criando bairro temporário (modo debug).");
            this.bairro = new Bairro();
        }

        addBehaviour(new TickerBehaviour(this, tickPeriod) {
            @Override
            protected void onTick() {
                List<PersonAgent> aInfectar = AbstractFSMBehavior.getAInfectarNoTick();

                synchronized (aInfectar) {
                    if (!aInfectar.isEmpty()) {
                        LoggerSMA.system("\n=== [Controlador] Processando infecções do tick %d ===", tickAtual);
                        for (PersonAgent p : aInfectar) {
                            if (!p.isInfectado()) {
                                p.infectar(p.getDoenca());
                                LoggerSMA.event(p, "💉 %s foi infectado no tick %d", p.getLocalName(), tickAtual);
                            }
                        }
                        aInfectar.clear();
                    }
                }

                // Atualiza estado do bairro e remove mortos
                if (bairro != null) {
                    bairro.removerAgentesMortos();
                }

                tickAtual++;
            }
        });
    }

    @Override
    protected void takeDown() {
        LoggerSMA.system("🛑 %s finalizado.", getLocalName());
    }
}
