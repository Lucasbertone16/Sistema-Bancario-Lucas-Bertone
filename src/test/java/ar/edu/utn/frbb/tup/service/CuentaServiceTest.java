package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.*;
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
    public void testCuentaSucess() throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException, TipoDeCuentaNoSoportadaException{
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(123654);
        long dniTitular = 48357988;

        when(cuentaDao.find(cuenta.getNumeroCuenta())).thenReturn(null); 

        cuentaService.darDeAltaCuenta(cuenta, dniTitular);

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
    public void testCuentaNotSupported() throws TipoDeCuentaNoSoportadaException {
        Cuenta cuentaNoSoportada = new Cuenta();
        cuentaNoSoportada.setTipoCuenta(TipoCuenta.TIPO_NO_SOPORTADO);
        long dniTitular = 123;

        assertThrows(TipoDeCuentaNoSoportadaException.class, () -> cuentaService.darDeAltaCuenta(cuentaNoSoportada,dniTitular));
    }

    @Test
   public void testClienteAlreadyHasCuentaType() throws TipoCuentaAlreadyExistsException, CuentaAlreadyExistsException, TipoDeCuentaNoSoportadaException{
        long dniTitular = 123;
        Cuenta cuenta = new Cuenta()
            .setMoneda(TipoMoneda.PESOS)
            .setBalance(500000)
            .setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        cuenta.setNumeroCuenta(112312);

        when(cuentaDao.find(cuenta.getNumeroCuenta())).thenReturn(null);

        cuentaService.darDeAltaCuenta(cuenta, dniTitular);

        Cuenta cuenta2 = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        cuenta2.setNumeroCuenta(321);

        
        assertThrows(TipoCuentaAlreadyExistsException.class, () -> cuentaService.darDeAltaCuenta(cuenta2, dniTitular));

        verify(cuentaDao, never()).save(cuenta2);
   }
}
