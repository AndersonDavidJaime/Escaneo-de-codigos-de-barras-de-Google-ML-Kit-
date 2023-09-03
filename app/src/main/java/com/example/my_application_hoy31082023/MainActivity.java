package com.example.my_application_hoy31082023;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.Manifest.permission;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnSuccessListener<Text>, OnFailureListener {

    public static int REQUEST_CAMERA = 111;
    public static int REQUEST_GALLERY = 222;

    Button btngaleria;
    Button btncamara;

    private Bitmap mSelectedImage;
    ImageView mImageView;

    TextView txtResults;

    Button btnCodigos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btngaleria = findViewById(R.id.btGallery);
        btncamara = findViewById(R.id.btCamera);
        mImageView = findViewById(R.id.image_view);
        txtResults = findViewById(R.id.txtresults);
        btnCodigos = findViewById(R.id.btnCodigos);
        add_events();
    }

    public void add_events() {
        btngaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_GALLERY);
            }
        });

        btncamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } catch (Exception ex) {
                }
            }
        });

        btnCodigos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                escanearCodigos();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            try {
                if (requestCode == REQUEST_CAMERA)
                    mSelectedImage = (Bitmap) data.getExtras().get("data");
                else
                    mSelectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                mImageView.setImageBitmap(mSelectedImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(android.Manifest.permission.CAMERA)) {
                btncamara.setEnabled(grantResults[i] == PackageManager.PERMISSION_GRANTED);
            } else if (permissions[i].equals(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE) ||
                    permissions[i].equals(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ) {
                btngaleria.setEnabled(grantResults[i] == PackageManager.PERMISSION_GRANTED);
            }
        }
    }

    public ArrayList<String> getPermisosNoAprobados(ArrayList<String> listaPermisos) {
        ArrayList<String> list = new ArrayList<String>();
        Boolean habilitado;
        if (Build.VERSION.SDK_INT >= 23)
            for (String permiso : listaPermisos) {
                if (checkSelfPermission(permiso) != PackageManager.PERMISSION_GRANTED) {
                    list.add(permiso);
                    habilitado = false;
                } else
                    habilitado = true;
                if (permiso.equals(android.Manifest.permission.CAMERA))
                    btncamara.setEnabled(habilitado);
                else if (permiso.equals(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE) ||
                        permiso.equals(android.Manifest.permission.READ_EXTERNAL_STORAGE))
                    btngaleria.setEnabled(habilitado);
            }
        return list;
    }
    @Override
    public void onFailure(@NonNull Exception e) {
        txtResults.setText("Error al procesar imagen");
    }

    @Override
    public void onSuccess(Text text) {
        List<Text.TextBlock> blocks = text.getTextBlocks();
        String resultados = "";
        if (blocks.size() == 0) {
            resultados = "No hay Texto";
        } else {
            for (int i = 0; i < blocks.size(); i++) {
                List<Text.Line> lines = blocks.get(i).getLines();
                for (int j = 0; j < lines.size(); j++) {
                    List<Text.Element> elements = lines.get(j).getElements();
                    for (int k = 0; k < elements.size(); k++) {
                        resultados = resultados + elements.get(k).getText() + " ";
                    }
                }
            }
            resultados = resultados + "\n";
        }
        txtResults.setText(resultados);
    }



    private void escanearCodigos() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_AZTEC,
                        Barcode.FORMAT_CODABAR,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39,
                        Barcode.FORMAT_CODE_93,
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_ITF,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E,
                        Barcode.FORMAT_PDF417,
                        Barcode.FORMAT_DATA_MATRIX
                )
                .build();
        InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
        BarcodeScanner scanner = BarcodeScanning.getClient(options);
        Task<List<Barcode>> result = scanner.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        procesarCodigosDeBarras(barcodes);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        txtResults.setText("Error al escanear códigos: " + e.getMessage());
                    }
                });
    }
    private void procesarCodigosDeBarras(List<Barcode> barcodes) {
        if (barcodes.size() > 0) {
            StringBuilder resultText = new StringBuilder("Resultados:\n");
            for (Barcode barcode : barcodes) {
                Rect bounds = barcode.getBoundingBox();
                Point[] corners = barcode.getCornerPoints();
                String rawValue = barcode.getRawValue();
                int valueType = barcode.getValueType();
                resultText.append("Tipo de código: ").append(valueType).append("\n");
                resultText.append("Valor del código: ").append(rawValue).append("\n");
                // Para acceder a la información específica del tipo de código
                switch (valueType) {
                    case Barcode.TYPE_WIFI:
                        String ssid = barcode.getWifi().getSsid();
                        String password = barcode.getWifi().getPassword();
                        int encryptionType = barcode.getWifi().getEncryptionType();
                        resultText.append("SSID WiFi: ").append(ssid).append("\n");
                        resultText.append("Contraseña WiFi: ").append(password).append("\n");
                        resultText.append("Tipo de encriptación WiFi: ").append(encryptionType).append("\n");
                        break;
                    case Barcode.TYPE_URL:
                        String title = barcode.getUrl().getTitle();
                        String url = barcode.getUrl().getUrl();
                        resultText.append("Título URL: ").append(title).append("\n");
                        resultText.append("URL: ").append(url).append("\n");
                        break;
                    default:
                        //Cuando el tipo de código no reconocido
                        break;
                }
            }
            txtResults.setText(resultText.toString());
        } else {
            txtResults.setText("No se encontraron códigos.");
        }
    }
}
