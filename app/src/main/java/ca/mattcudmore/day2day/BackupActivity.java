package ca.mattcudmore.day2day;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ca.mattcudmore.day2day.db.D2dDatabase;
import timber.log.Timber;

public class BackupActivity extends AppCompatActivity {

	private static final int BACKUP = 0, RESTORE = 1;

	private final File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
	private final File backupFile = new File(downloadsDir, "d2d-backup.sql");
	private File databaseFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup);

		databaseFile = getDatabasePath(D2dDatabase.DB_NAME);

		((TextView) findViewById(R.id.textView_backupLocation))
				.setText(getString(R.string.textView_backupLocation_text, backupFile.getAbsolutePath()));

		findViewById(R.id.button_backupDatabase).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				tryBackup();
			}
		});

		findViewById(R.id.button_restoreDatabase).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				tryRestore();
			}
		});
	}

	private void tryBackup() {
		if (!requireWritePermission(BACKUP)) {
			return;
		}
		if (backupFile.exists()) {
			new AlertDialog.Builder(BackupActivity.this)
					.setTitle(R.string.button_backupDatabase_text)
					.setMessage(R.string.dialog_backupDatabase_overwrite)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							exportBackupDatabase();
						}
					})
					.setNegativeButton(android.R.string.cancel, null)
					.show();
		} else {
			exportBackupDatabase();
		}
	}

	private void tryRestore() {
		if (!requireWritePermission(RESTORE)) {
			return;
		}
		if (backupFile.exists()) {
			new AlertDialog.Builder(this)
					.setTitle(R.string.button_restoreDatabase_text)
					.setMessage(R.string.dialog_restoreDatabase_confirm)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							importBackupDatabase();
						}
					})
					.setNegativeButton(android.R.string.cancel, null)
					.show();
		} else {
			Toast.makeText(this, R.string.toast_restoreDatabase_noFile, Toast.LENGTH_LONG).show();
		}
	}

	private boolean requireWritePermission(int requestCode) {
		if (Build.VERSION.SDK_INT >= 23) {
			if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				return true;
			}
			requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
			return false;
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			switch (requestCode) {
				case BACKUP:
					tryBackup();
					break;
				case RESTORE:
					tryRestore();
					break;
			}
		} else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
			Toast.makeText(this, R.string.toast_permissionDenied, Toast.LENGTH_LONG).show();
		}
	}

	private void exportBackupDatabase() {
		try {
			backupFile.delete();
			backupFile.getParentFile().mkdirs();
			backupFile.createNewFile();
			copyFile(databaseFile, backupFile);
			Toast.makeText(this, getString(R.string.toast_backupDatabase_success, backupFile.getAbsolutePath()), Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Timber.e(e, "Backup failed");
			Toast.makeText(this, R.string.toast_backupDatabase_failed, Toast.LENGTH_LONG).show();
		}
	}

	private void importBackupDatabase() {
		try {
			databaseFile.delete();
			databaseFile.createNewFile();
			copyFile(backupFile, databaseFile);
			backupFile.delete();
			Toast.makeText(this, R.string.toast_restoreDatabase_success, Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Timber.e(e, "Restore failed");
			Toast.makeText(this, R.string.toast_restoreDatabase_failed, Toast.LENGTH_LONG).show();
		}
	}

	private void copyFile(File from, File to) throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(from);
			out = new FileOutputStream(to);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				Timber.e(e);
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				Timber.e(e);
			}
		}
	}

}
