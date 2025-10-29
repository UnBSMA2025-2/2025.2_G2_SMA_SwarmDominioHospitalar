package hospital.agents;

import hospital.model.Bairro;
import hospital.model.Doenca;
import hospital.behaviors.ElderFSMBehavior;
import jade.core.behaviours.TickerBehaviour;

import java.util.Random;

public class ElderAgent extends PersonAgent {

    protected Random rand = new Random();

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

    @Override
    protected void configurarVulnerabilidade() {
        double v = Math.pow(rand.nextDouble(), 0.5); // maioria alta, raros baixos
        this.vulnerabilidade = 0.2 + v * 0.8; // faixa total 0.2–1.0
    }
}