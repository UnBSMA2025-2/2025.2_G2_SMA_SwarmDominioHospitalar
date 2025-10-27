package hospital.agents;

import hospital.model.Bairro;
import hospital.model.Doenca;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public abstract class PersonAgent extends Agent {

    protected Bairro bairro;
    protected int posX = 0;
    protected int posY = 0;
    protected int homeX;
    protected int homeY;
    protected boolean casaDefinida = false;
    protected boolean infectado = false;
    protected Doenca doenca;

    // ===================== POSIÇÃO =====================
    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public void setPos(int x, int y) { this.posX = x; this.posY = y; }

    public int getHomeX() { return homeX; }
    public int getHomeY() { return homeY; }
    public void setCasa(int x, int y) { this.homeX = x; this.homeY = y; }

    public boolean isCasaDefinida() { return casaDefinida; }
    public void setCasaDefinida(boolean casaDefinida) { this.casaDefinida = casaDefinida; }

    public boolean isInfectado() { return infectado; }

    public void setInfectado(boolean infectado) {
        this.infectado = infectado;
    }

    public Doenca getDoenca() { return doenca; }

    // ===================== SETUP COMUM =====================
    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.bairro = (Bairro) args[0];
        }

        // Configura doença padrão (subclasses podem alterar)
        this.doenca = criarDoenca();

        System.out.println(getEmoji() + " " + getLocalName() + " criado(a)!");
        adicionarAoBairro();

        addBehaviour(criarBehaviour());
    }

    // ===================== MÉTODOS ABSTRATOS PARA SUBCLASSES =====================
    protected abstract TickerBehaviour criarBehaviour();
    protected abstract void adicionarAoBairro();
    protected abstract Doenca criarDoenca();
    protected abstract String getEmoji();

    @Override
    protected void takeDown() {
        System.out.println("😴 " + getLocalName() + " encerrou suas atividades.");
    }
}