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
<img width="451" height="559" alt="image" src="https://github.com/user-attachments/assets/fb5e608d-3626-4210-977f-9b2d09c65917" />

---

## 🧰 Tecnologias Utilizadas

- **Kotlin**
- **Jetpack Compose (Material 3)**
- **Firebase Authentication**
- **Firebase Firestore**
- **Android Keystore**
- **BiometricPrompt**
- **Credential Manager (Google Sign-In)**
- **Coroutines & StateFlow**

---

## 🔑 Configuração do Firebase

Este repositório **não inclui** o arquivo `google-services.json` por motivos de segurança.

Para rodar o projeto localmente:

1. Crie um projeto no Firebase Console
2. Ative:
   - Authentication (Google + SMS)
   - Firestore
3. Baixe o `google-services.json`
4. Coloque o arquivo em: `app/google-services.json`

> Certifique-se de restringir sua API Key por **Package Name + SHA-1** no Google Cloud Console.

---

## 🚀 Status do Projeto

- ✅ Funcional
- ✅ Estável para uso pessoal/familiar
- 🧊 Funcionalidades congeladas (v1.0)

Novas funcionalidades só devem ser adicionadas se houver necessidade real de uso.

---

## 📄 Licença

Este projeto é de uso pessoal e educacional.  
Sinta-se à vontade para estudar, adaptar ou evoluir o código conforme sua necessidade.

---

## 🙌 Considerações Finais

O PassGuardian foi desenvolvido com foco em **segurança prática**, evitando complexidade desnecessária e priorizando a experiência de usuários comuns.
