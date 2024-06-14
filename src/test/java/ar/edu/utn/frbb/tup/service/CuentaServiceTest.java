package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.*;
import ar.edu.utn.frbb.tup.model.exception.ClienteAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoDeCuentaNoSoportadaException;
import ar.edu.utn.frbb.tup.persistence.ClienteDao;
import ar.edu.utn.frbb.tup.persistence.CuentaDao;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CuentaServiceTest {
    @Mock
    private CuentaDao cuentaDao;

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private CuentaService cuentaService;

    @BeforeAll
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCuentaSucess() throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException, TipoDeCuentaNoSoportadaException, ClienteAlreadyExistsException {
        Cliente cliente = new Cliente();
        cliente.setDni(46339672);

        clienteService.darDeAltaCliente(cliente);

        Cuenta cuenta = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE)
                .setBalance(150000);
        cuenta.setNumeroCuenta(2203);

        cuentaService.darDeAltaCuenta(cuenta, cliente.getDni());

        verify(cuentaDao, times(1)).save(cuenta);

    }

    @Test
    public void testCuentaAlreadyExists() throws CuentaAlreadyExistsException {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(999);
        long dniTitular = 12345678;

        when(cuentaDao.find(cuenta.getNumeroCuenta())).thenReturn(new Cuenta());    //Retorna Cuenta porque debe indicar que la cuenta ya existe

        assertThrows(CuentaAlreadyExistsException.class, () -> cuentaService.darDeAltaCuenta(cuenta, dniTitular));

        verify(cuentaDao, never()).save(any(Cuenta.class)); //Verifico que nunca se haya llamado el metodo save
    }

    @Test
    public void testCuentaNoSoportada() {
        Cuenta cuentaNoSoportada = new Cuenta();
        cuentaNoSoportada.setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE);
        cuentaNoSoportada.setMoneda(TipoMoneda.DOLARES);

        when(cuentaDao.find(cuentaNoSoportada.getNumeroCuenta())).thenReturn(null);

        assertThrows(TipoDeCuentaNoSoportadaException.class, () -> cuentaService.darDeAltaCuenta(cuentaNoSoportada, 123));
    }

    @Test    //    3 - cliente ya tiene cuenta de ese tipo
    public void testClienteAlreadyHasCuentaType() throws TipoCuentaAlreadyExistsException, CuentaAlreadyExistsException, TipoDeCuentaNoSoportadaException{
        long dniTitular = 38944251;


        Cuenta cuenta = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE)
                .setBalance(150000);
        cuenta.setNumeroCuenta(12345);


        Cuenta cuenta2 = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE)
                .setBalance(150000);
        cuenta2.setNumeroCuenta(12346);


        when(cuentaDao.find(cuenta.getNumeroCuenta())).thenReturn(null);
        when(cuentaDao.find(cuenta2.getNumeroCuenta())).thenReturn(null);

        cuentaService.darDeAltaCuenta(cuenta, dniTitular);


        doThrow(new TipoCuentaAlreadyExistsException("El cliente ya tiene una cuenta de este tipo."))
                .when(clienteService).agregarCuenta(any(Cuenta.class), eq(dniTitular));

        // Verificar que se lanza la excepción correcta al intentar agregar una cuenta del mismo tipo
        assertThrows(TipoCuentaAlreadyExistsException.class, () -> cuentaService.darDeAltaCuenta(cuenta2, dniTitular));

        // Verificar que el método save del DAO no se haya llamado para la segunda cuenta
        verify(cuentaDao, never()).save(cuenta2);
    }
}
