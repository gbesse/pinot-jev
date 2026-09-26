# Pinot Jev

An [Apache Pinot](https://pinot.apache.org/) scalar-function extension for semantic predicates backed by [TypeSafe Jev](https://docs.typesafe.ai/api). It fuses up to eight conditions over one row into one request, caches identical judgments on each Pinot server, and coalesces simultaneous identical calls. This initial release makes no state-of-the-art performance claim.

## Français

### Requête

```sql
SELECT review_id, body
FROM reviews
WHERE rating >= 3
  AND jevAll(body, '["La critique parle de la fin du film", "La critique recommande le film"]') = 1;
```

`jevAll` exige toutes les conditions ; `jevAny` en exige une ; `jevProbability(texte, condition)` renvoie la probabilité d'une seule condition. Seuil des fonctions booléennes : 0,5. Un texte `NULL` ne déclenche pas de requête et renvoie 0.

### Installer et vérifier

Avec Java 17 et Maven :

```sh
mvn --batch-mode verify
```

Déposer `target/pinot-jev-0.1.0.jar` dans le répertoire `/plugins` des instances Pinot qui exécutent les requêtes, définir `JEV_API_KEY` dans leur environnement, puis les redémarrer. Pinot découvre les méthodes publiques annotées `@ScalarFunction` dans `org.apache.pinot.function.jev`. Vérifier le chargement de la fonction sur une instance de test avant déploiement dans un cluster. Une clé dans le client SQL ne configure pas les serveurs Pinot.

### Coût et limites

Chaque couple inédit texte/conditions sur un serveur déclenche une requête HTTPS. Le cache est local au processus (4 096 entrées, environ 8 Mio de clés), sans partage entre nœuds ni persistance. La limite par défaut est de 10 000 appels par processus ; une requête distribuée peut solliciter plusieurs processus. Aucun index sémantique ni traitement par lots entre lignes. Réduire les lignes candidates avec les filtres classiques et mesurer le plan et le coût. Texte et conditions sont transmis à TypeSafe.

## English

### Query

```sql
SELECT review_id, body
FROM reviews
WHERE rating >= 3
  AND jevAll(body, '["The review discusses the ending", "The review recommends the movie"]') = 1;
```

`jevAll` requires every condition; `jevAny` requires one; `jevProbability(text, condition)` returns the probability for one condition. Boolean threshold: 0.5. A `NULL` text does not make a request and returns 0.

### Install and verify

With Java 17 and Maven:

```sh
mvn --batch-mode verify
```

Place `target/pinot-jev-0.1.0.jar` in `/plugins` on Pinot instances that execute the queries, set `JEV_API_KEY` in their process environment, and restart them. Pinot discovers public methods annotated `@ScalarFunction` in `org.apache.pinot.function.jev`. Verify function loading on a test instance before cluster deployment. A key set in the SQL client does not configure Pinot servers.

### Cost and limits

Each new text/conditions pair on a server makes one HTTPS request. Cache is process-local (4,096 entries and about 8 MiB of keys), neither shared across nodes nor persistent. The default limit is 10,000 calls per process; a distributed query may use multiple processes. There is no semantic index or cross-row batching. Narrow candidates with ordinary filters and measure the plan and cost. Text and conditions are sent to TypeSafe.

## Español

### Consulta

```sql
SELECT review_id, body
FROM reviews
WHERE rating >= 3
  AND jevAll(body, '["La reseña comenta el final de la película", "La reseña recomienda la película"]') = 1;
```

`jevAll` exige todas las condiciones; `jevAny` exige una; `jevProbability(texto, condición)` devuelve la probabilidad de una condición. Umbral booleano: 0,5. Un texto `NULL` no envía una solicitud y devuelve 0.

### Instalar y comprobar

Con Java 17 y Maven:

```sh
mvn --batch-mode verify
```

Coloque `target/pinot-jev-0.1.0.jar` en `/plugins` de las instancias Pinot que ejecutan las consultas, configure `JEV_API_KEY` en el entorno de sus procesos y reinícielas. Pinot detecta métodos públicos con `@ScalarFunction` en `org.apache.pinot.function.jev`. Verifique la carga de las funciones en una instancia de prueba antes de desplegar en un clúster. Una clave definida en el cliente SQL no configura los servidores Pinot.

### Coste y límites

Cada pareja nueva de texto y condiciones en un servidor genera una solicitud HTTPS. La caché es local al proceso (4.096 entradas y aproximadamente 8 MiB de claves), sin compartir entre nodos ni persistencia. El límite predeterminado es 10.000 llamadas por proceso; una consulta distribuida puede usar varios procesos. No hay índice semántico ni lotes entre filas. Reduzca las filas candidatas con filtros normales y mida el plan y el coste. Los textos y condiciones se envían a TypeSafe.

## Configuration / Configuration / Configuración

| Variable | Default / Défaut / Predeterminado |
| --- | --- |
| `JEV_API_KEY` or `TYPESAFE_API_KEY` | required / requis / obligatorio |
| `JEV_MODEL` | `jev-1.13.0` |
| `JEV_API_URL` | `https://api.typesafe.ai/v1/systemone` |
| `JEV_TIMEOUT_MS` | `10000` |
| `JEV_MAX_REQUESTS` | `10000` |

The tests use synthetic data and a mock API; they verify protocol, cache, concurrency and Pinot annotations, not live model accuracy or cluster throughput.

Les tests utilisent des données synthétiques et une API simulée ; ils vérifient le protocole, le cache, la concurrence et les annotations Pinot, pas la précision réelle du modèle ni le débit en cluster.

Las pruebas usan datos sintéticos y una API simulada; verifican el protocolo, la caché, la concurrencia y las anotaciones Pinot, no la precisión real del modelo ni el rendimiento de un clúster.

Licence MIT. Sans affiliation avec TypeSafe ou Apache Pinot.

MIT license. Not affiliated with TypeSafe or Apache Pinot.

Licencia MIT. Sin afiliación con TypeSafe ni Apache Pinot.
