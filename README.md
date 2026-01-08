<div align="center">
  <img width="500" height="500" alt="logo" src="https://github.com/user-attachments/assets/cc44da97-147e-4c60-8393-647f40f8f08c" />
</div>

# 🔐 PassGuardian

**PassGuardian** é um aplicativo Android de gerenciamento de senhas, desenvolvido para uso pessoal e familiar, com foco em **segurança local**, **simplicidade de uso** e **boa experiência para usuários não técnicos**.

O app foi projetado para proteger senhas sensíveis sem depender de servidores próprios, utilizando autenticação forte, criptografia no dispositivo e validações adicionais de acesso.

---

## ✨ Principais Funcionalidades

- 🔑 Login com conta Google
- 📲 Autenticação em dois fatores (SMS via Firebase, quando exigido)
- 🧬 Biometria como camada adicional de segurança
- 🔐 Cofre de senhas criptografado localmente
- 📋 Copiar senha ou usuário com um toque
- 👁️ Visualização temporária da senha (10 segundos)
- 🔁 Auto-lock ao sair do aplicativo
- 🚫 Bloqueio de screenshots e preview em apps recentes
- 🧹 Limpeza de dados sensíveis da memória ao sair das telas
- 🎨 Interface simples, moderna e amigável

---

## 🛡️ Segurança (Visão Geral)

O PassGuardian foi pensado para **reduzir ao máximo o risco de vazamento de dados**, mesmo em cenários comuns como perda ou acesso físico ao dispositivo.

### Medidas adotadas:
- 🔒 **Criptografia no cliente** usando Android Keystore
- 📦 Senhas **nunca são armazenadas em texto puro**
- 🧬 Descriptografia acontece apenas em memória e sob demanda
- 🔐 Biometria e/ou verificação por SMS para acessar ou editar dados
- ⏱️ Sessão desbloqueada por tempo limitado
- 🚫 Screenshots desabilitados em todo o app

> ⚠️ Observação: este aplicativo **não tem suporte a sincronização multi-dispositivo**.  
> Cada instalação mantém seu próprio cofre criptografado.

---

## 🧭 Fluxo do Aplicativo

1. Login com Google
2. Verificação adicional por SMS (se exigido pela conta)
3. Acesso à lista de senhas
4. Para abrir ou editar uma senha:
   - Biometria (ou fallback)
5. Visualização, cópia ou edição segura
6. Ao sair do app → auto-lock automático

---

## 🧱 Arquitetura

O projeto segue uma arquitetura simples e organizada:

