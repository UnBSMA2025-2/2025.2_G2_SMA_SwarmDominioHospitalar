package hospital.agents;

import hospital.model.Doenca;
import jade.core.Agent;
import hospital.model.Bairro;
import hospital.behaviors.ElderFSMBehavior;

public class ElderAgent extends Agent {

    private Bairro bairro;
    private int posX = 0;
    private int posY = 0;
    private int homeX;
    private int homeY;
    private boolean casaDefinida = false;
    private boolean infectado = false;
    private Doenca doenca;

    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public void setPos(int x, int y) { this.posX = x; this.posY = y; }

    public int getHomeX() { return homeX; }
    public int getHomeY() { return homeY; }
    public void setCasa(int x, int y) { this.homeX = x; this.homeY = y; }

    public boolean isCasaDefinida() { return casaDefinida; }
    public void setCasaDefinida(boolean casaDefinida) { this.casaDefinida = casaDefinida; }

    public Bairro getBairro() { return bairro; }

    public boolean isInfectado() { return infectado; }
    public void setInfectado(boolean infectado) {
        this.infectado = infectado;
        if (infectado) {
            System.out.println("😷 [" + getLocalName() + "] foi infectado!");
        }
    }

    @Override
    protected void setup() {
        Object[] args = getArguments();
        String descricao = "(sem descrição)";

        if (args != null && args.length > 0) {
            this.bairro = (Bairro) args[0]; // bairro compartilhado
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                sb.append(args[i].toString()).append(" | ");
            }
            descricao = sb.toString();
        }

        System.out.println("🧑‍💼 " + getLocalName() + " foi criado! Descrição: " + descricao);

        // Adiciona o agente ao bairro
        bairro.adicionarAgenteElder(this);

        // Comportamento com movimentação e infecção
        addBehaviour(new ElderFSMBehavior(this, 1000, bairro));
    }

    public Doenca getDoenca() {
        return this.doenca;
    }


    @Override
    protected void takeDown() {
        System.out.println("😴 " + getLocalName() + " encerrou suas atividades e foi desligado.");
    }


}
