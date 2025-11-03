package hospital.model;

/**
 * Representa a cidade composta por uma grade de bairros.
 * Cada bairro contém sua própria matriz 4x4 de locais e agentes.
 */
public class Cidade {

    private final Bairro[][] bairros;
    private final int linhasBairro;
    private final int colunasBairro;

    /**
     * Cria uma cidade com a quantidade de bairros informada.
     * Exemplo: new Cidade(2, 2) cria uma cidade 2x2.
     */
    public Cidade(int linhasBairro, int colunasBairro) {
        this.linhasBairro = linhasBairro;
        this.colunasBairro = colunasBairro;
        this.bairros = new Bairro[linhasBairro][colunasBairro];

        // Inicializa todos os bairros automaticamente
        for (int i = 0; i < linhasBairro; i++) {
            for (int j = 0; j < colunasBairro; j++) {
                bairros[i][j] = new Bairro();
            }
        }
    }

    /** Retorna a matriz completa de bairros. */
    public Bairro[][] getBairros() {
        return bairros;
    }

    /** Retorna o bairro em uma posição específica. */
    public Bairro getBairro(int linha, int coluna) {
        if (linha < 0 || linha >= linhasBairro || coluna < 0 || coluna >= colunasBairro) {
            throw new IndexOutOfBoundsException("Índice do bairro inválido: (" + linha + "," + coluna + ")");
        }
        return bairros[linha][coluna];
    }

    /** Retorna o número total de linhas de bairros. */
    public int getLinhasBairro() {
        return linhasBairro;
    }

    /** Retorna o número total de colunas de bairros. */
    public int getColunasBairro() {
        return colunasBairro;
    }

    /** Retorna uma string resumindo a estrutura da cidade. */
    @Override
    public String toString() {
        return String.format("Cidade %dx%d com %d bairros.", linhasBairro, colunasBairro, linhasBairro * colunasBairro);
    }
}
