# Rush Hour Puzzle Solver

**Rush Hour Puzzle Solver** adalah sebuah program pencarian solusi untuk sebuah permasalahan Rush Hour. Pencarian dilakukan dengan menggunakan algoritma pathfinding Greedy Best-first Search, Uniform Cost Search, dan A* untuk mencari solusi. Program ini dibuat untuk memenuhi Tugas Kecil 3 mata kuliah Strategi Algoritma (IF2211).

## Requirement dan Instalasi
Program ini berjalan dengan bahasa Java 21 dengan dependency JavaFX 21.0.3, dan dibangun dengan struktur Maven. Aplikasi sudah dikompilasi dan executable dapat digunakan selama Java dapat berjalan di perangkat dengan versi yang sesuai.

- [Java 21](https://www.oracle.com/id/java/technologies/downloads/#java21)

Untuk melakukan kompilasi, Maven atau library JavaFX harus diinstal di perangkat terlebih dahulu.

- [Maven Apache](https://maven.apache.org/install.html)
- [JavaFX 21.0.3](https://gluonhq.com/products/javafx/)

## Cara Mengkompilasi Program
Build dapat dilakukan dengan dua cara terpisah. Dapat menggunakan Maven atau menggunakan Javac.
### Maven
Jalankan skrip berikut di directory yang mengandung pom.xml untuk menginstal dependensi yang dibutuhkan dan menghasilkan file executable di bin.
  ```
  mvn clean install
  ```

Setelah proses build selesai, file executable .jar akan tersedia di bin.
### Javac
Jalankan skrip berikut di directory yang mengandung src untuk melakukan kompilasi. Pastikan javafx-sdk sudah dapat diakses di dalam perangkat dengan version yang sesuai

```
javac --module-path "PATH\TO\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml -d bin src/main/java/tucil/rhsolver/app/*.java
```

## Cara Menjalankan dan Menggunakan Program
Setelah build selesai, jalankan executable melalui terminal/command prompt dari direktori utama dengan format:

```bash
java -jar .\bin\rush-hour-puzzle-solver-jar-with-dependencies.jar
```

Jika tidak melakukan kompilasi dengan Maven, jalankan skrip berikut untuk memulai program.
```bash
java --module-path "PATH\TO\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml -cp bin src.main.java.tucil.rhsolver.app.AppLauncher

```

Setelah program berhasil berjalan, pengguna dapat memasukkan input board dalam bentuk .txt ataupun secara manual melalui GUI yang tersedia.

Jika mengalami kesulitan dalam proses build atau tidak memenuhi requirement, pengguna dapat langsung menggunakan aplikasi yang dibarengi rilis kami.

## Author
Program ini dibuat oleh:
- Samuel Gerrard Hamonangan Girsang 13523064
- Lutfi Hakim Yusra 13523084

## Credits
Repository ini memanfaatkan template repo berikut:
- [javafx-jar-template](https://github.com/FDelporte/javafx-jar-template)

Repository ini juga merupakan hasil perpindahan repositori ini ke dalam template:
- [Tucil3_1352306_13523084-deprecated](https://github.com/pixelatedbus/Tucil3_13523064_13523084-deprecated)




