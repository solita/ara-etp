LibreOffice
===
[LibreOffice][libre] is used to generate pdf-documents from excel-files.

The LibreOffice configuration is defined in `./config`-folder.

In desktop installations this configuration is typically located e.g.:
```
~/.config/libreoffice/4
~/snap/libreoffice/204/.config/libreoffice/4
```

Here libreoffice is executed in a headless mode with environment flag:
```
"-env:UserInstallation=file://" tmpdir "/config"
```
Before execution this config folder is copied to the temporary folder: `tmpdir`.

More details can be found from [here](../src/main/clj/solita/common/libreoffice.clj).

See also: https://stackoverflow.com/questions/59987439/running-libreoffice-as-a-service

[libre]: www.libreoffice.org


