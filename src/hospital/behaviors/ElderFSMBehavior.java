package hospital.behaviors;

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
        return switch (tickDoDia) {
            case 0 -> Local.TRABALHO;
            case 1 -> {
                int escolha = rand.nextInt(3);
                yield switch (escolha) {
                    case 0 -> Local.FESTA;
                    case 1 -> Local.PARQUE;
                    default -> Local.CASA;
                };
            }
            default -> Local.CASA;
        };
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
