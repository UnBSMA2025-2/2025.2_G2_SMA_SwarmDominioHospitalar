package hospital.agents;

import hospital.model.Bairro;
import jade.core.Agent;
import hospital.behaviors.ChildFSMBehavior;
import hospital.model.Doenca;

public class ChildAgent extends Agent {

    private boolean infectado = false;
    private Doenca doenca;
    private Bairro bairro;
    private int posX = 0;
    private int posY = 0;
    private int homeX;
    private int homeY;
    private boolean casaDefinida = false;

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPos(int x, int y) {
        this.posX = x;
        this.posY = y;
    }

    @Override
    protected void setup() {
        Object[] args = getArguments();

        String descricao = "(sem descrição)";
        if (args != null && args.length > 0) {
            String[] dados = (String[]) args[0];    // descrição da criança
            this.bairro = (Bairro) args[1];        // bairro compartilhado
            boolean pacienteZero = (args.length > 2) && (boolean) args[2]; // flag paciente zero
            this.infectado = pacienteZero;         // já infectado se for paciente zero

            StringBuilder sb = new StringBuilder();
            for (String d : dados) sb.append(d).append(" | ");
            descricao = sb.toString();

            if (pacienteZero) {
                System.out.println("⚠️ " + getLocalName() + " começou infectado! (Paciente Zero)");
            }
        }

        this.doenca = new Doenca("COVID", 0.1, 2.0);
        System.out.println("👶 " + getLocalName() + " foi criado! Descrição: " + descricao);

        // Adiciona o agente à lista global do bairro
        bairro.adicionarAgenteChild(this);

        // Comportamento com movimentação e infecção
        addBehaviour(new ChildFSMBehavior(this, 1000, bairro));
    }

    public Bairro getBairro() {
        return bairro;
    }

    public boolean isInfectado() {
        return infectado;
    }

    public void setInfectado(boolean infectado) {
        this.infectado = infectado;
        if (infectado) {
            System.out.println("😷 [" + getLocalName() + "] foi infectado com " + doenca.getNome() + "!");
        }
    }

    public Doenca getDoenca() {
        return doenca;
    }

    public void setCasa(int x, int y) {
        this.homeX = x;
        this.homeY = y;
    }

    public int getHomeX() {
        return homeX;
    }

    public int getHomeY() {
        return homeY;
    }

    public boolean isCasaDefinida() {
        return casaDefinida;
    }

    public void setCasaDefinida(boolean casaDefinida) {
        this.casaDefinida = casaDefinida;
    }

    @Override
    protected void takeDown() {
        System.out.println("👋 " + getLocalName() + " terminou seu dia e está sendo desligado.");
    }
}