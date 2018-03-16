package br.com.caelum.leilao.servico;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.LeilaoDao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;

public class EncerradorDeLeilaoTest {

	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAtras() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de Plasma").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();

		RepositorioDeLeiloes dao = mock(LeilaoDao.class);
		EnviadorDeEmail carteiro = mock(EnviadorDeEmail.class);
		when(dao.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(dao, carteiro);
		encerrador.encerra();

		assertEquals(2, encerrador.getTotalEncerrados());
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());
		
		InOrder inOrder = inOrder(dao, carteiro);
		inOrder.verify(dao, times(1)).atualiza(leilao1);
		inOrder.verify(carteiro, times(1)).envia(leilao1);
		inOrder.verify(dao, times(1)).atualiza(leilao2);
		inOrder.verify(carteiro, times(1)).envia(leilao2);
		
	}

	@Test
	public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {

		Calendar ontem = Calendar.getInstance();
		ontem.add(Calendar.DAY_OF_MONTH, -1);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(ontem).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(ontem).constroi();

		RepositorioDeLeiloes dao = mock(LeilaoDao.class);
		EnviadorDeEmail carteiro = mock(EnviadorDeEmail.class);
		when(dao.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(dao, carteiro);
		encerrador.encerra();

		assertEquals(0, encerrador.getTotalEncerrados());
		assertFalse(leilao1.isEncerrado());
		assertFalse(leilao2.isEncerrado());
		
		verify(dao, never()).atualiza(leilao1);
		verify(dao, never()).atualiza(leilao2);
	}
	
	@Test
	public void naoDeveEncerrarLeiloesCasoNaoHajaNenhum() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		RepositorioDeLeiloes dao = mock(LeilaoDao.class);
		EnviadorDeEmail carteiro = mock(EnviadorDeEmail.class);
		when(dao.correntes()).thenReturn(new ArrayList<Leilao>());

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(dao, carteiro);
		encerrador.encerra();

		assertEquals(0, encerrador.getTotalEncerrados());
	}
	
	@Test
	public void deveAtualizarLeiloesEncerrados() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de Plasma").naData(antiga).constroi();

		RepositorioDeLeiloes dao = mock(LeilaoDao.class);
		EnviadorDeEmail carteiro = mock(EnviadorDeEmail.class);
		when(dao.correntes()).thenReturn(Arrays.asList(leilao1));

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(dao, carteiro);
		encerrador.encerra();

		verify(dao, times(1)).atualiza(leilao1);
	}
	
}
