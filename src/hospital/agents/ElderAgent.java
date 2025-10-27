package hospital.agents;

import hospital.model.Bairro;
import hospital.model.Doenca;
import hospital.behaviors.ElderFSMBehavior;
import jade.core.behaviours.TickerBehaviour;

public class ElderAgent extends PersonAgent {

    @Override
    protected TickerBehaviour criarBehaviour() {
        return new ElderFSMBehavior(this, 1000, bairro);
    }

    @Override
    protected void adicionarAoBairro() {
        bairro.adicionarAgenteElder(this);
    }

    @Override
    protected Doenca criarDoenca() {
        return new Doenca("COVID", 0.1, 2.0);
    }

    @Override
    protected String getEmoji() {
        return "🧓";
    }
}