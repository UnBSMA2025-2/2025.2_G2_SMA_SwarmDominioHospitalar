package hospital.agents;

import hospital.model.Bairro;
import hospital.model.Doenca;
import hospital.behaviors.AdultFSMBehavior;
import jade.core.behaviours.TickerBehaviour;

import java.util.Random;

public class AdultAgent extends PersonAgent {

    protected Random rand = new Random();

    @Override
    protected TickerBehaviour criarBehaviour() {
        return new AdultFSMBehavior(this, 1000, bairro);
    }

    @Override
    protected void adicionarAoBairro() {
        bairro.adicionarAgenteAdult(this);
    }

    @Override
    protected Doenca criarDoenca() {
        return new Doenca("COVID", 0.1, 2.0);
    }

    @Override
    protected String getEmoji() {
        return "🧑‍💼";
    }

    @Override
    protected void configurarVulnerabilidade() {
        double v = rand.nextGaussian() * 0.15 + 0.5; // média 0.5, desvio 0.15
        this.vulnerabilidade = Math.max(0.0, Math.min(1.0, v)); // limita entre 0–1
    }


}
