package com.example.dolorders.activity;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dolorders.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AboutActivity extends AppCompatActivity {

    private static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Ajouter le bouton retour dans l'ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("À propos");
        }

        LinearLayout pdfContainer = findViewById(R.id.pdfContainer);

        try {
            File pdfFile = new File(getCacheDir(), "testNotice.pdf");
            // Supprime l'ancien fichier s'il existe
            if (pdfFile.exists()) {
                pdfFile.delete();
            }

            InputStream is = getAssets().open("testNotice.pdf");
            FileOutputStream fos = new FileOutputStream(pdfFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.close();
            is.close();

            // Ouvre le PDF avec PdfRenderer
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);

            // Parcourt toutes les pages du PDF
            for (int i = 0; i < pdfRenderer.getPageCount(); i++) {
                PdfRenderer.Page page = pdfRenderer.openPage(i);

                // Crée un bitmap pour la page
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth() * 2, page.getHeight() * 2, Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                // Crée une ImageView pour afficher la page
                ImageView imageView = new ImageView(this);
                imageView.setImageBitmap(bitmap);
                imageView.setAdjustViewBounds(true);
                imageView.setPadding(0, 0, 0, 16);
                pdfContainer.addView(imageView);

                page.close();
            }

            pdfRenderer.close();
            fileDescriptor.close();

        } catch (IOException e) {
            Log.e(TAG, "Erreur lors du chargement du PDF", e);
            Toast.makeText(this, "Erreur lors du chargement du PDF", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
