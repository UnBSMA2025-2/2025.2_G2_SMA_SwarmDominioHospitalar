package hospital.enums;

public enum Local {
    CASA("Casa"),
    ESCOLA("Escola"),
    TRABALHO("Trabalho"),
    PARQUE("Parque"),
    ATIVIDADE("Atividade"),
    FESTA("Festa"),
    FIM("FimDoDia");

    private final String descricao;

    Local(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}