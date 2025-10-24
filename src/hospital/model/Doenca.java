package hospital.model;

public class Doenca {
    private String nome;
    private double beta;       // taxa de transmissão base
    private double infectividade; // infectividade do agente doente

    public Doenca(String nome, double beta, double infectividade) {
        this.nome = nome;
        this.beta = beta;
        this.infectividade = infectividade;
    }

    public String getNome() {
        return nome;
    }

    public double getBeta() {
        return beta;
    }

    public double getInfectividade() {
        return infectividade;
    }
}
