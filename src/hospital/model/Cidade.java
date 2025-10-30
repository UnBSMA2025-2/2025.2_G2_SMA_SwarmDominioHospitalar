package hospital.model;

public class Cidade {

    private Bairro[][] bairros;

    public Cidade(int linhasBairro, int colunasBairro) {
        bairros = new Bairro[2][2];

        bairros[0][0] = new Bairro();
        bairros[0][1] = new Bairro();
        bairros[1][0] = new Bairro();
        bairros[1][1] = new Bairro();

    }

    public  Bairro[][] getBairros() {
        return bairros;
    }

    public Bairro getBairro(int linha, int coluna) {
        if(linha <0 || linha >1 || coluna <0 || coluna >2) {
            throw new IndexOutOfBoundsException("Índice do bairro inválido");
        }
        return bairros[linha][coluna];
    }

}