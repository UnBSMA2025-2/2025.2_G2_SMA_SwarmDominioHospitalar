package hospital.behaviors;

import hospital.agents.AdultAgent;
import hospital.agents.ElderAgent;
import hospital.enums.Local;
import hospital.model.Bairro;

import java.util.ArrayList;
import java.util.List;

public class ElderFSMBehavior extends AbstractFSMBehavior<ElderAgent> {

    public ElderFSMBehavior(ElderAgent agente, long period, Bairro bairro) {
        super(agente, period, bairro);
    }

    @Override
    protected Local definirLocalDoDia(ElderAgent agente, int tickDoDia) {
        AdultAgent.GravidadeSintoma sintoma = agente.getSintomaAtual();

        if (sintoma == AdultAgent.GravidadeSintoma.MORTE) {
            myAgent.doDelete();
            return null;
        }

        if (sintoma == AdultAgent.GravidadeSintoma.GRAVE) {
            return Local.CASA;
        }

        // Rotina para idosos - geralmente ficam em casa, mas podem ir ao parque a tarde
        switch (tickDoDia) {
            case 0:
            case 2: return Local.CASA;      // manhã e noite em casa
            case 1: return Local.PARQUE;    // tarde no parque
            default: return Local.CASA;
        }
    }

    @Override
    protected int[] encontrarCasaDisponivel(ElderAgent agente) {
        List<int[]> casasLivres = new ArrayList<>();
        for (int i = 0; i < bairro.getLinhas(); i++) {
            for (int j = 0; j < bairro.getColunas(); j++) {
                if (bairro.getLocal(i, j) == Local.CASA && bairro.getAgentesNoLocalElder(i, j).isEmpty()) {
                    casasLivres.add(new int[]{i, j});
                }
            }
        }
        if (casasLivres.isEmpty()) return new int[]{0, 0};
        return casasLivres.get(rand.nextInt(casasLivres.size()));
    }
}
