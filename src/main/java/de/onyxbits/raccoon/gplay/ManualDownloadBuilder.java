/*
 * Copyright 2015 Patrick Ahlbrecht
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.onyxbits.raccoon.gplay;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.akdeniz.googleplaycrawler.GooglePlay.AppDetails;
import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlay.DocV2.Builder;
import com.akdeniz.googleplaycrawler.GooglePlay.DocumentDetails;

import de.onyxbits.raccoon.gui.ButtonBarBuilder;
import de.onyxbits.raccoon.gui.DialogBuilder;
import de.onyxbits.raccoon.transfer.TransferManager;
import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.swing.ActionLocalizer;

/**
 * For starting a download by directly entering parameters.
 * 
 * @author patrick
 * 
 */
public class ManualDownloadBuilder extends AbstractPanelBuilder implements
		ActionListener, PlayListener {

	/**
	 * ID for referencing this builder.
	 */
	public static final String ID = ManualDownloadBuilder.class.getSimpleName();

	private JButton download;
	private JTextField packId;
	private JSpinner versionCode;
	private JSpinner offerType;
	private JRadioButton free;
	private JRadioButton paid;

	public ManualDownloadBuilder() {
		ActionLocalizer al = Messages.getLocalizer();
		packId = new JTextField(25);
		packId.setMargin(new Insets(2, 2, 2, 2));
		versionCode = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE,
				1));
		versionCode.setEditor(new JSpinner.NumberEditor(versionCode, "#"));
		offerType = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
		paid = new JRadioButton(al.localize("paid_app"));
		free = new JRadioButton(al.localize("free_app"));
	}

	@Override
	protected JPanel assemble() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		ActionLocalizer al = Messages.getLocalizer();
		download = new JButton(al.localize("appdownload"));

		ButtonGroup bg = new ButtonGroup();
		bg.add(free);
		bg.add(paid);
		free.setSelected(true);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets.right = 5;
		gbc.insets.bottom = 3;
		panel.add(new JLabel(Messages.getString(ID + ".packageid")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets.right = 0;
		panel.add(packId, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.insets.right = 5;
		panel.add(new JLabel(Messages.getString(ID + ".versioncode")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.insets.right = 0;
		panel.add(versionCode, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.insets.right = 5;
		//panel.add(new JLabel(Messages.getString(ID + ".offertype")), gbc);
		panel.add(Box.createGlue(), gbc);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.insets.right = 0;
		panel.add(Box.createGlue(), gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.insets.right = 5;
		panel.add(new JLabel(Messages.getString(ID + ".flow")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.insets.right = 0;
		panel.add(free, gbc);

		gbc.gridx = 2;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.insets.right = 5;
		panel.add(paid, gbc);

		gbc.gridx = 2;
		gbc.gridy = 4;
		gbc.insets.right = 0;
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets.top = 20;
		gbc.insets.bottom = 0;
		panel.add(download, gbc);

		download.addActionListener(this);

		globals.get(PlayManager.class).addPlayListener(this);
		return new DialogBuilder(panel)
				.withButtons(new ButtonBarBuilder().add(download))
				.withTitle(Messages.getString(ID + ".title"))
				.withSubTitle(Messages.getString(ID + ".subtitle")).build(globals);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Builder builder = DocV2.newBuilder().setBackendDocid(packId.getText())
				.setTitle(packId.getText());
		builder.addOfferBuilder().setOfferType((Integer) offerType.getValue())
				.setCheckoutFlowRequired(paid.isSelected());
		builder.setDetails(DocumentDetails.newBuilder().setAppDetails(
				AppDetails.newBuilder()
						.setVersionCode((Integer) versionCode.getValue())));
		globals.get(TransferManager.class).schedule(globals,
				new AppDownloadWorker(globals, builder.build()), TransferManager.WAN);
		packId.setText("");
		versionCode.setValue(1);
		offerType.setValue(1);
		free.setSelected(true);
	}

	@Override
	public void onAppSearch() {
	}

	@Override
	public void onAppSearchResult(List<DocV2> apps, boolean append) {
	}

	@Override
	public void onAppView(DocV2 app, boolean brief) {
		packId.setText(app.getBackendDocid());
		versionCode.setValue(app.getDetails().getAppDetails().getVersionCode());
		if (app.getOfferCount() > 0) {
			offerType.setValue(app.getOffer(0).getOfferType());
			paid.setSelected(app.getOffer(0).getCheckoutFlowRequired());
		}
	}
}
