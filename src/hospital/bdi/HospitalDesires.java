package hospital.bdi;

public class HospitalDesires {
    private boolean desejaCurarPacientes = true;
    private boolean desejaEconomizarRecursos = true;
    private boolean desejaEvitarLotacao = true;

    public boolean querCurar() { return desejaCurarPacientes; }
    public boolean querEconomizar() { return desejaEconomizarRecursos; }
    public boolean querEvitarLotacao() { return desejaEvitarLotacao; }

    public void setCurarPacientes(boolean valor) { desejaCurarPacientes = valor; }
}

