# GLRegion

Módulo LSPosed que libera a lista completa de módulos do **Good Lock** em aparelhos Samsung onde a lista vem reduzida (por exemplo, a linha Galaxy A no Brasil), falsificando o modelo do aparelho apenas dentro do Good Lock e da Galaxy Store.

## Como funciona

O servidor da Samsung (`getContentCategoryProductList.as`) filtra a lista de módulos pelo **modelo do aparelho** (`deviceId`), e não pelo IP, pelo MCC/MNC ou pelo csc.

O Good Lock monta o `deviceId` a partir de `Build.MODEL`. O hook troca esse valor por `SM-S938B` (Galaxy S25 Ultra) somente nos processos alvo. O resto do sistema continua enxergando o aparelho real.

### Pacotes alvo

| Pacote | Por quê |
| --- | --- |
| `com.samsung.android.goodlock` | Pede a lista de módulos |
| `com.sec.android.app.samsungapps` | Galaxy Store; a etapa de download (`stubDownload.as`) reenvia o `deviceId` |

## Diagnóstico (como chegamos nisso)

Foi usada a requisição completa, com todos os parâmetros (`contentCategoryID`, `deviceId`, `sdkVer`, `mcc`, `mnc`, `csc`, `imgWidth`, `imgHeight`, `startNum`, `endNum`, `alignOrder`, `callerId`, `cc`, `systemId`, `abiType`, `oneUiVersion`), variando um parâmetro por vez e contando os `<appInfo>` devolvidos:

| Teste | Módulos |
| --- | --- |
| Valores reais (`SM-A166B`, 724/05, XAA) | 3 |
| Só o MCC/MNC trocado | 3 |
| Só o csc trocado | 3 |
| Tudo trocado (`SM-S938B`, 262/01, INS) | 23 |
| **Só o modelo trocado (`SM-S938B`)** | **23** |
| Só o modelo trocado (`SM-A566B`) | 21 |

Conclusão: o modelo sozinho define a lista. O `SM-S938B` devolve todos os 23 módulos.

No código decompilado do Good Lock (`cf/h0.java`), o `deviceId` vem de `Build.MODEL`, removendo um prefixo `SAMSUNG-` se existir. Já `xe/i.java` `c()` e `d()` devolvem o nome do pacote e a versão instalada, sem relação com o modelo.

## Uso

1. Compile o módulo:

   ```sh
   ./gradlew assembleDebug
   ```

2. Instale o APK:

   ```sh
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

3. No LSPosed, ative o módulo e marque no escopo os dois pacotes da tabela acima.
4. Force a parada do Good Lock e da Galaxy Store (ou reinicie o aparelho).
5. Abra o Good Lock. A lista deve mostrar os 23 módulos.
6. No log do LSPosed, procure a linha `GLRegion: modelo falso SM-S938B`.

### Conferência do módulo

- `app/src/main/assets/xposed_init` deve conter exatamente `com.xkzetsu.glregion.Hook`.
- A primeira linha de `Hook.kt` deve ser `package com.xkzetsu.glregion`.
- O `AndroidManifest.xml` precisa dos metadados `xposedmodule`, `xposeddescription` e `xposedminversion`.

## Teste do servidor (`teste.sh`)

O script `teste.sh` consulta o servidor com diferentes modelos e mostra quantos módulos cada um recebe. Rode quando uma atualização do Good Lock ou da Galaxy Store quebrar o hook, para saber se o servidor mudou o filtro.

```sh
bash teste.sh
```

Precisa de `adb` com o aparelho conectado (para ler `ro.product.model`) e `curl`.

## Se parar de funcionar

- **A lista volta a ter 3 módulos e o log mostra o hook:** o app pode ter passado a ignorar `Build.MODEL`. Procure no código decompilado por `f19323e` em `cf/h0.java`, que é um override do modelo vindo de configuração.
- **Nenhuma linha no log:** módulo desativado ou escopo errado.
- **Lista completa, mas o download falha:** confira se o hook também está ativo na Galaxy Store.
- **Um módulo instalado reclama do aparelho:** o módulo faz a própria checagem de compatibilidade, e o hook não cobre isso.

## Limitações

- O hook só altera o modelo visto pelo Good Lock e pela Galaxy Store. Módulos instalados que checam o aparelho por conta própria podem recusar a execução.
- Depende de a Samsung continuar filtrando por modelo. Atualizações podem mudar isso.

## Estrutura

```
app/src/main/java/com/xkzetsu/glregion/Hook.kt   # o hook
app/src/main/assets/xposed_init                  # classe de entrada do módulo
teste.sh                                         # teste de contagem no servidor
```
