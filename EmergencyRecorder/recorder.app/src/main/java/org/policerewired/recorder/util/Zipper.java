package org.policerewired.recorder.util;

import android.content.Context;
import android.net.Uri;

import org.apache.commons.io.IOUtils;
import org.policerewired.recorder.constants.AuditRecordType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Manages writing files to a chosen Zip file.
 */
public class Zipper {

  public static final String mime_type_zip = "application/zip";

  private Context context;
  private File zip_file;
  private List<String> zipped_files;

  private boolean open;
  private ZipOutputStream out_stream;

  public Zipper(Context context, File zipFile) {
    this.context = context;
    this.zip_file = zipFile;
    zipped_files = new LinkedList<>();
  }

  public boolean isOpen() { return open; }

  public List<String> getZippedFiles() { return zipped_files; }

  public File getZipFile() { return zip_file; }

  public void open() throws IOException {
    if (open) { throw new IllegalStateException("Already open."); }
    out_stream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip_file)));
    open = true;
  }

  public void close() throws IOException {
    if (!open) { throw new IllegalStateException("Not open, cannot close."); }
    out_stream.flush();
    out_stream.close();
    open = false;
  }

  public void addStream(InputStream in_stream, String internal_folder, String filename) throws IOException {
    ZipEntry entry = new ZipEntry(internal_folder + "/" + filename);
    out_stream.putNextEntry(entry);
    IOUtils.copy(in_stream, out_stream);
    zipped_files.add(filename);
  }

  public void addFile(String internal_folder, File source) throws IOException {
    if (!open) { throw new IllegalStateException("Not open, cannot add file."); }
    addStream(new BufferedInputStream(new FileInputStream(source)), internal_folder, source.getName());
  }

  public static String getFilename(Date time, AuditRecordType type, Uri sourceUri) {
    return String.format("%s_%s_%s.%s",
      type.name(), // filetype,
      String.valueOf(time.getTime()), // date
      sourceUri.getLastPathSegment(), // uri last part
      type.filename_suffix); // suffix
  }

}
