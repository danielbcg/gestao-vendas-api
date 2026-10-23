import React, { useState, useEffect } from 'react';
import ImportarPlanilha from './components/ImportarPlanilha';
import NovaVenda from './components/NovaVenda';
import VendasRegistradas from './components/VendasRegistradas';

export default function App() {
  const [abaAtiva, setAbaAtiva] = useState('vendas');
  const [iaConfigurada, setIaConfigurada] = useState(false);
  const [produtos, setProdutos] = useState([]);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const carregarProdutos = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/produtos');
      if (res.ok) {
        const data = await res.json();
        setProdutos(data);
      }
    } catch (err) {
      console.warn('Não foi possível carregar o catálogo de produtos no momento.');
    }
  };

  const verificarStatusIa = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/ia/status');
      if (res.ok) {
        const data = await res.json();
        setIaConfigurada(Boolean(data.configurado));
      }
    } catch (err) {
      setIaConfigurada(false);
    }
  };

  useEffect(() => {
    carregarProdutos();
    verificarStatusIa();
  }, [refreshTrigger]);

  const dispararAtualizacao = () => {
    setRefreshTrigger(prev => prev + 1);
  };

  return (
    <div className="app-container">
      {/* Header */}
      <header className="app-header">
        <div>
          <h1 className="brand-title">Gestão Fácil de Vendas ✨</h1>
          <p className="brand-subtitle">Loja de Acessórios da Maria</p>
        </div>

        <div className="ia-status-pill">
          <span className={`status-dot ${iaConfigurada ? 'active' : 'inactive'}`}></span>
          <span>{iaConfigurada ? 'Inteligência Artificial Pronta' : 'IA Não Configurada'}</span>
        </div>
      </header>

      {/* Navegação por abas */}
      <nav className="tabs-nav">
        <button
          className={`tab-btn ${abaAtiva === 'vendas' ? 'active' : ''}`}
          onClick={() => setAbaAtiva('vendas')}
        >
          📋 Vendas Registradas
        </button>
        <button
          className={`tab-btn ${abaAtiva === 'nova-venda' ? 'active' : ''}`}
          onClick={() => setAbaAtiva('nova-venda')}
        >
          ➕ Nova Venda
        </button>
        <button
          className={`tab-btn ${abaAtiva === 'importar' ? 'active' : ''}`}
          onClick={() => setAbaAtiva('importar')}
        >
          📥 Importar Planilha
        </button>
      </nav>

      {/* Conteúdo da aba */}
      <main>
        {abaAtiva === 'importar' && (
          <ImportarPlanilha
            iaConfigurada={iaConfigurada}
            produtos={produtos}
            onVendasAtualizadas={dispararAtualizacao}
          />
        )}

        {abaAtiva === 'nova-venda' && (
          <NovaVenda
            produtos={produtos}
            onProdutoCriado={carregarProdutos}
            onVendaSalva={dispararAtualizacao}
          />
        )}

        {abaAtiva === 'vendas' && (
          <VendasRegistradas
            produtos={produtos}
            iaConfigurada={iaConfigurada}
            refreshTrigger={refreshTrigger}
            onVendaAtualizada={dispararAtualizacao}
          />
        )}
      </main>
    </div>
  );
}

