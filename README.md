# Control de Lanzamientos - interno

## Exportar hojas de vendedores a JSON/CSV

El directorio [`tools/`](tools/) incluye el script [`export_vendor_data.py`](tools/export_vendor_data.py) que
convierte cada hoja de vendedor de un archivo `.xlsx` en archivos JSON y CSV
normalizados. Se detectan automáticamente las hojas que contengan las columnas
`COD`, `Cliente/Cliente2` y `Localidad`. Para cada vendedor se generan dos
archivos en `exports/<vendedor>/`:

* `<vendedor>_<version>.json`: lista de clientes y los productos marcados con
  `1` en la hoja.
* `<vendedor>_<version>.csv`: filas normalizadas (una por producto comprado),
  listas para cargar en otra base de datos.

### Convención de versionado semanal

El script etiqueta la salida usando el formato `AAAA-Www`, basado en la semana
ISO de la fecha actual (por ejemplo `2024-W09`). Puede especificarse otro valor
mediante `--version` al ejecutar el comando.

### Requisitos

* Python 3.10+
* Dependencias:
  ```bash
  pip install openpyxl boto3
  ```
  `boto3` solo es necesario si se desea subir los archivos a S3.

### Uso

```bash
python tools/export_vendor_data.py <ruta/al/archivo.xlsx>
```

Opciones principales:

* `--output <directorio>`: Directorio de salida (por defecto `exports/`).
* `--version <tag>`: Define manualmente el identificador de carga semanal.
* `--s3-bucket <nombre>` y `--s3-prefix <prefijo>`: Sube los resultados al bucket
  indicado en Amazon S3. El script espera que las credenciales estén definidas en
  las variables de entorno de AWS o en el archivo de configuración habitual.

La salida puede compartirse con los 10 dispositivos mediante el bucket de S3 o
sincronizando el directorio `exports/` en un servicio interno accesible para
ellos.

### Manifiesto y próximos pasos

Cada ejecución genera un archivo `manifest_<version>.json` en la carpeta de
salida. El manifiesto incluye el `version` vigente, la marca de tiempo de
generación y los metadatos (conteo de clientes y productos) de cada vendedor.
Los dispositivos pueden descargar solo este archivo para detectar si hay una
carga nueva (comparando el campo `version`) antes de sincronizar los archivos
por vendedor.

Para automatizar la distribución semanal:

1. Ejecutar el script cada lunes con el libro actualizado.
2. Subir la carpeta `exports/` al almacenamiento elegido (por ejemplo S3) o
   colocarla en un recurso compartido interno.
3. Notificar a los dispositivos que consulten el manifiesto y descarguen solo
   las cargas cuyo `version` no hayan procesado aún.
