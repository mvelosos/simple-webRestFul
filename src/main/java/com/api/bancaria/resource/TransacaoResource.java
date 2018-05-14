package com.api.bancaria.resource;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.bancaria.model.Conta;
import com.api.bancaria.model.Transacao;
import com.api.bancaria.repository.ContaRepo;
import com.api.bancaria.repository.TransacaoRepo;
import com.api.bancaria.responses.Response;

@RestController
@RequestMapping("/transacao")
public class TransacaoResource {
	
	@Autowired
	private TransacaoRepo transacaoRepo;
	
	@Autowired
	private ContaRepo contaRepo;
	
	
	@GetMapping("/buscarTransacoes")
	public ResponseEntity<Response<List<Transacao>>> buscarTransacoes(){
		//return new ResponseEntity<List<Transacao>>(transacaoRepo.findAll(), HttpStatus.OK);
		return ResponseEntity.ok(new Response<List<Transacao>>(transacaoRepo.findAll()));
	}
	
	@PostMapping("/depositar")
	public ResponseEntity<Transacao> depositar(@Valid @RequestBody Transacao transacao){
		
		BigDecimal min = new BigDecimal(0);
		Conta conta = contaRepo.findOne(transacao.getIdConta().getIdConta());
		
		if(conta.getFlagAtivo() == true && (transacao.getValor().compareTo(min) > 0)) {
			BigDecimal novoSaldo = conta.getSaldo().add(transacao.getValor());
			conta.setSaldo(novoSaldo);
			contaRepo.save(conta);
			
			return new ResponseEntity<Transacao>(transacaoRepo.save(transacao), HttpStatus.CREATED);
		}
		return ResponseEntity.badRequest().build();
	}
	
	@PostMapping("/sacar")
	public ResponseEntity<Transacao> sacar(@Valid @RequestBody Transacao transacao){
		System.out.println(transacao.getIdConta().getIdConta());
		Conta conta = contaRepo.findOne(transacao.getIdConta().getIdConta());
		if((conta.getFlagAtivo() == true) && (transacao.getValor().compareTo(conta.getLimiteSaqueDiario()) <= 0)
				&& (transacao.getValor().compareTo(conta.getSaldo()) <= 0)) {
			BigDecimal novoLimite = conta.getLimiteSaqueDiario().subtract(transacao.getValor());
			conta.setLimiteSaqueDiario(novoLimite);
			BigDecimal novoSaldo = conta.getSaldo().subtract(transacao.getValor());
			conta.setSaldo(novoSaldo);
			contaRepo.save(conta);
			
			return new ResponseEntity<Transacao>(transacaoRepo.save(transacao), HttpStatus.CREATED);
		}
		return ResponseEntity.badRequest().build();
	}
	
	@GetMapping("/extrato/{idConta}")
	public ResponseEntity<Response<List<Transacao>>> extrato(@PathVariable Conta idConta){
	
		return ResponseEntity.ok(new Response<List<Transacao>>(transacaoRepo.findByIdConta(idConta)));
	}
}