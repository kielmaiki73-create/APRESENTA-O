package com.mk.crystal.installer;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.zip.*;
public class MainActivity extends Activity {
 private LinearLayout root; private TextView status; private ImageView preview; private Button save,wall,lock; private File theme; private boolean busy=false;
 private int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
 private int c(String s){return Color.parseColor(s);}
 private GradientDrawable glass(){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{c("#FFFFFF"),c("#DCEFFC"),c("#BBDDF5")});g.setCornerRadius(dp(22));g.setStroke(dp(1),c("#FFFFFF"));return g;}
 private TextView label(String s,int n){TextView v=new TextView(this);v.setText(s);v.setTextSize(n);v.setTextColor(c("#153C60"));v.setPadding(dp(8),dp(8),dp(8),dp(8));return v;}
 private Button button(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setTextColor(c("#164568"));b.setAllCaps(false);b.setTextSize(15);b.setBackground(glass());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(56));p.bottomMargin=dp(10);root.addView(b,p);b.setOnClickListener(v->r.run());return b;}
 @Override public void onCreate(Bundle b){super.onCreate(b);theme=new File(getFilesDir(),"selected.mtz");getWindow().setStatusBarColor(c("#CBE7FA"));getWindow().setNavigationBarColor(c("#E6F4FD"));getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
 ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(24),dp(24),dp(24),dp(24));root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{c("#D6EEFF"),c("#FFFFFF"),c("#C4E1F5")}));sc.addView(root);setContentView(sc);
 TextView mk=label("MK",52);mk.setTypeface(Typeface.DEFAULT,Typeface.BOLD_ITALIC);mk.setGravity(Gravity.CENTER);root.addView(mk);
 TextView t=label("CRYSTAL GLASS",24);t.setGravity(Gravity.CENTER);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);root.addView(t);
 TextView sub=label("Assistente de temas • HyperOS",13);sub.setGravity(Gravity.CENTER);root.addView(sub);
 status=label("Selecione o arquivo .mtz já baixado.",15);root.addView(status);
 preview=new ImageView(this);preview.setScaleType(ImageView.ScaleType.CENTER_CROP);preview.setBackground(glass());preview.setClipToOutline(true);root.addView(preview,new LinearLayout.LayoutParams(-1,dp(200)));
 button("1. Selecionar tema MTZ",()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,10);});
 save=button("2. Exportar MTZ para Arquivos",()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/octet-stream");i.putExtra(Intent.EXTRA_TITLE,"MK-Crystal-Glass.mtz");startActivityForResult(i,11);});
 button("3. Abrir Temas da Xiaomi",()->{Intent i=getPackageManager().getLaunchIntentForPackage("com.android.thememanager");if(i!=null)startActivity(i);else tell("Aplicativo Temas não encontrado.");});
 wall=button("Aplicar papel de parede • Inicial",()->apply(WallpaperManager.FLAG_SYSTEM,"wallpaper/default_wallpaper.jpg"));
 lock=button("Aplicar papel de parede • Bloqueio",()->apply(WallpaperManager.FLAG_LOCK,"wallpaper/default_lock_wallpaper.jpg"));
 root.addView(label("Importação integral de ícones e sistema depende do aplicativo Temas. Se a opção Importar não existir, este assistente não pode liberar a função. Papel de parede pode ser aplicado separadamente. Sem root.",13));
 refresh();}
 private void tell(String s){status.setText(s);Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 private void refresh(){boolean ok=theme.isFile()&&!busy;save.setEnabled(ok);wall.setEnabled(ok);lock.setEnabled(ok);if(ok){try(ZipFile z=new ZipFile(theme)){ZipEntry e=z.getEntry("preview/en_US_launcher_0.jpg");if(e==null)e=z.getEntry("wallpaper/default_wallpaper.jpg");if(e!=null&&e.getSize()<=12000000){try(InputStream in=z.getInputStream(e)){BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=2;preview.setImageBitmap(BitmapFactory.decodeStream(in,null,o));}}status.setText("Tema selecionado. Exporte ou aplique o papel de parede.");}catch(Exception e){tell("Não foi possível ler a prévia.");}}}
 private static void copy(InputStream i,OutputStream o,long limit)throws IOException{byte[] buf=new byte[16384];long total=0;int n;while((n=i.read(buf))!=-1){total+=n;if(total>limit)throw new IOException("Arquivo excede limite");o.write(buf,0,n);}}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null||busy)return;Uri uri=data.getData();busy=true;refresh();new Thread(()->{String message;try{
 if(request==10){File tmp=new File(getFilesDir(),"pending.mtz");try(InputStream in=getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(tmp)){if(in==null)throw new IOException("Arquivo indisponível");copy(in,out,150L*1024*1024);}try(ZipFile z=new ZipFile(tmp)){if(z.getEntry("description.xml")==null)throw new IOException("Não é um tema MTZ válido");}if(!tmp.renameTo(theme))throw new IOException("Não foi possível salvar");message="Tema MTZ reconhecido.";}
 else if(request==11){try(InputStream in=new FileInputStream(theme);OutputStream out=getContentResolver().openOutputStream(uri)){if(out==null)throw new IOException("Destino indisponível");copy(in,out,150L*1024*1024);}message="MTZ exportado. Abra Temas para tentar importar.";}else message="Operação cancelada.";
 }catch(Exception e){message="Erro: "+e.getMessage();}final String m=message;runOnUiThread(()->{busy=false;refresh();tell(m);});}).start();}
 private void apply(int flag,String path){if(busy)return;busy=true;refresh();new Thread(()->{String message;try(ZipFile z=new ZipFile(theme)){ZipEntry e=z.getEntry(path);if(e==null||e.getSize()<0||e.getSize()>12000000)throw new IOException("Papel de parede ausente ou muito grande");WallpaperManager w=WallpaperManager.getInstance(this);if(!w.isWallpaperSupported()||!w.isSetWallpaperAllowed())throw new IOException("Sistema não permite alterar");try(InputStream in=z.getInputStream(e)){w.setStream(in,null,true,flag);}message="Papel de parede aplicado.";}catch(Exception e){message="Erro: "+e.getMessage();}final String m=message;runOnUiThread(()->{busy=false;refresh();tell(m);});}).start();}
}
