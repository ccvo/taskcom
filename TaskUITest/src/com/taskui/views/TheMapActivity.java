package com.taskui.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.taskui.models.Constants;

public final class TheMapActivity extends MapActivity {

	private MapView mapView;
	private MyItemizedOverlay itemizedOverlay; // itemizedOverlay is a subclass of overlay.
	private static final int ZOOM_LEVEL = 15;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_mapview);
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setZoom(ZOOM_LEVEL);

		Button btnBack = (Button) findViewById(R.id.backButton);
		Button btnNext = (Button) findViewById(R.id.nextButton);
		Button btnShare = (Button) findViewById(R.id.shareButton);
		Button btnSkip = (Button) findViewById(R.id.skipButton);

		btnBack.setEnabled(getIntent().getBooleanExtra(Constants.KEY_IS_BACK_BUTTON, false));
		btnShare.setEnabled(getIntent().getBooleanExtra(Constants.KEY_IS_SHARE_BUTTON, false));
		btnSkip.setEnabled(getIntent().getBooleanExtra(Constants.KEY_IS_SKIP_BUTTON, false));
		btnNext.setText(getIntent().getBooleanExtra(Constants.KEY_IS_NEXT_BUTTON, false) ? R.string.next_button_label : R.string.done_button_label);

		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK, new Intent().putExtra(Constants.KEY_BUTTON_CLICKED, Constants.KEY_IS_NEXT_BUTTON));
				finish();
			}
		});

		btnSkip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK, new Intent().putExtra(Constants.KEY_BUTTON_CLICKED, Constants.KEY_IS_SKIP_BUTTON));
				finish();
			}
		});

		Drawable red_dot = this.getResources().getDrawable(R.drawable.red_dot);
		itemizedOverlay = new MyItemizedOverlay(red_dot);
		mapView.getOverlays().add(itemizedOverlay);

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Toast.makeText(this, "Your GPS is not enabled! Please enable GPS to update your current location.", Toast.LENGTH_LONG).show();
		}

		String address = getIntent().getStringExtra(Constants.KEY_ADDRESS);
		new GetAddressList().execute(address);
	}

	private final class GetAddressList extends AsyncTask<String, Void, Object> {
		private ProgressDialog progressDialog;
		private String address;

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(TheMapActivity.this, null, "Loading, please wait...", true, false);
			progressDialog.show();
		}

		@Override
		protected Object doInBackground(String... _address) {
			this.address = _address[0];
			Geocoder geocoder = new Geocoder(TheMapActivity.this);
			//getting the lastKnownLocation
			try {
				LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (lastKnownLocation == null) {
					lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				}
				if (lastKnownLocation != null) {
					return geocoder.getFromLocationName(this.address, 5, lastKnownLocation.getLatitude() - 1, lastKnownLocation.getLongitude() - 1,
							lastKnownLocation.getLatitude() + 1, lastKnownLocation.getLongitude() + 1);
				} else {
					return geocoder.getFromLocationName(this.address, 5);
				}
			} catch (IOException e) {
				return e;
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			progressDialog.dismiss();
			if (result instanceof Exception) {
				Exception e = (Exception) result;
				AlertDialog.Builder builder = new AlertDialog.Builder(TheMapActivity.this);
				builder.setTitle("Error");
				builder.setMessage(e.getMessage());
				builder.setPositiveButton("OK", null);
				AlertDialog alertDialog = builder.create();
				alertDialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						finish();
					}
				});
				alertDialog.show();
			} else {
				@SuppressWarnings("unchecked")
				final List<Address> addresses = (List<Address>) result;
				if (addresses == null || addresses.isEmpty()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(TheMapActivity.this);
					builder.setTitle("Error");
					builder.setMessage("Could not find this place '" + this.address + "'!");
					builder.setPositiveButton("OK", null);
					AlertDialog alertDialog = builder.create();
					alertDialog.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							finish();
						}
					});
					alertDialog.show();
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(TheMapActivity.this);
					String[] items = new String[addresses.size()];
					boolean check = false;
					for (int i = 0; i < addresses.size(); i++) {
						String addressLine = "";
						int maxAddressLineIndex = addresses.get(i).getMaxAddressLineIndex();
						if (maxAddressLineIndex != -1) {
							for (int j = 0; j <= addresses.get(i).getMaxAddressLineIndex(); j++) {
								addressLine += addresses.get(i).getAddressLine(j) + " ";
							}
						}
						items[i] = addressLine;
						check = true;
					}
					if (!check) {
						builder.setTitle("Error");
						builder.setMessage("Could not find this place '" + this.address + "'!");
						builder.setPositiveButton("OK", null);
						AlertDialog alertDialog = builder.create();
						alertDialog.setOnDismissListener(new OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								finish();
							}
						});
						alertDialog.show();
					} else {
						OnClickListener onClickListener = new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Address a = addresses.get(which);
								GeoPoint point = new GeoPoint((int) (a.getLatitude() * 1E6), (int) (a.getLongitude() * 1E6));
								dialog.dismiss();
								makeUseOfNewLocation(point, address);
							}
						};
						builder.setTitle("Select your place:");
						builder.setOnCancelListener(new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								finish();
							}
						});
						builder.setSingleChoiceItems(items, -1, onClickListener).show();
					}
				}
			}
		}
	}

	private void makeUseOfNewLocation(GeoPoint point, String _address) {
		//update the map
		OverlayItem overlayitem = new OverlayItem(point, _address, "");
		if (itemizedOverlay.size() == 0) {
			itemizedOverlay.addOverlay(overlayitem);
		} else {
			itemizedOverlay.updateOverlay(0, overlayitem);
		}
		mapView.getController().animateTo(point);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return true;
	}

	private final class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

		public MyItemizedOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}

		public void addOverlay(OverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
		}

		public void updateOverlay(int i, OverlayItem overlay) {
			mOverlays.set(i, overlay);
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return mOverlays.get(i);
		}

		@Override
		public int size() {
			return mOverlays.size();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}