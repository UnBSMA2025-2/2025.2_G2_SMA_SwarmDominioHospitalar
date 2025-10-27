package hospital.behaviors;

import hospital.agents.ChildAgent;
import hospital.enums.Local;
import hospital.model.Bairro;

import java.util.ArrayList;
import java.util.List;

public class ChildFSMBehavior extends AbstractFSMBehavior<ChildAgent> {

    public ChildFSMBehavior(ChildAgent agente, long period, Bairro bairro) {
        super(agente, period, bairro);
    }

    @Override
    protected Local definirLocalDoDia(ChildAgent agente, int tickDoDia) {
        return switch (tickDoDia) {
            case 0 -> Local.ESCOLA;
            case 1 -> rand.nextBoolean() ? Local.PARQUE : Local.ATIVIDADE;
            default -> Local.CASA;
        };
    }

    @Override
    protected int[] encontrarCasaDisponivel(ChildAgent agente) {
        List<int[]> casasLivres = new ArrayList<>();
        for (int i = 0; i < bairro.getLinhas(); i++) {
            for (int j = 0; j < bairro.getColunas(); j++) {
                if (bairro.getLocal(i, j) == Local.CASA && bairro.getAgentesNoLocalChild(i, j).isEmpty()) {
                    casasLivres.add(new int[]{i, j});
                }
            }
        }
        if (casasLivres.isEmpty()) return new int[]{0, 0};
        return casasLivres.get(rand.nextInt(casasLivres.size()));
    }
}