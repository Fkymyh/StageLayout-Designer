package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class HelpDialog extends JDialog {

    private static final String HELP_TEXT =
            """
            StageLayout Designer 使い方

            ■ 基本の流れ
            1. ファイル > 新規 で作業シートの大きさを選びます。
            2. イベント情報でイベント名、日付、会場、担当者、メモを入れます。
            3. 左の機材パレットから機材を選びます。
            4. キャンバスをダブルクリックして機材を置きます。
            5. 置いた機材はドラッグで移動できます。
            6. プレビューで提出用の見た目を確認します。
            7. 保存で作業データを残します。

            ■ よく使う操作
            ・選択した機材はドラッグで移動できます。
            ・Rキー、または回転ボタンで向きを変えられます。
            ・+ / - キーで大きさを変えられます。
            ・Deleteキーで選択中の機材を消せます。
            ・Ctrl+Zで戻る、Ctrl+Yでやり直しができます。
            ・ツールバーの保存ボタンで今のファイルを保存できます。

            ■ 線・バミリ線
            「線を引く」では、クリックした点から次にクリックした点へ線を引けます。
            「バミリ線」では、赤いテープ用の線を引けます。バミリ線にはメートル表示は出ません。
            Shiftを押しながら引くと、縦か横にそろえやすくなります。
            Ctrlを押しながら引くと、1m単位に合わせやすくなります。

            線を消す時は、線をクリックして選択してからDeleteキー、または右クリックの削除を使います。
            線を引いている途中でも、既にある線を右クリックすれば削除できます。
            選択中の線は、両端の赤い点をドラッグして位置を直せます。

            ■ 背景図面
            ホールや会場のフロアマップ画像を背景図面として読み込めます。
            背景図面は下絵として表示され、その上に機材、人物、線、テキストを配置できます。
            現在はPNG/JPG/JPEG形式に対応しています。
            PDFはPDFBoxの追加が必要なため、現時点では読み込み時に案内を表示します。

            背景図面を固定すると、誤って移動しないようになります。
            機材や線を配置するときは、背景図面を固定しておくと作業しやすくなります。

            ■ テキストボックス
            文字モードを使うと、図面上に任意の文字を配置できます。
            受付、音響卓、出演者待機、立入禁止などの補足説明に利用できます。
            配置した文字はドラッグで移動できます。
            文字をダブルクリックすると内容を編集できます。

            ■ 機材パレットとモード
            線描画モードや文字モード中でも、機材パレットから機材を選ぶと自動で機材配置モードに戻ります。

            ■ 会場テンプレート
            会場は最初から特別な編集モードに切り替えなくても作れます。
            左のパレットの 舞台 > 会場パーツ から四角形や円形を置いて会場を作ります。

            会場ができたら ファイル > 会場だけ保存 で保存します。
            保存した会場は ファイル > 会場テンプレートを読み込み で使えます。

            会場固定をONにすると、会場パーツを動かしにくくできます。
            当日の仕込み図では、会場を固定して、その上に機材を置くイメージです。

            ■ お気に入り
            よく使う機材は、機材ボタンを右クリックしてお気に入りに追加できます。
            お気に入りはこのアプリ全体で共通です。

            ■ プレビュー
            プレビューでは、提出用の配置図、必要機材一覧、メモをまとめて確認できます。
            PNG画像として保存することも、印刷することもできます。

            ■ 困った時
            ・作業場所が広すぎる時は、表示倍率を「全体」にします。
            ・置いたものが動かない時は、会場固定がONになっていないか確認します。
            ・会場を作る時は、まず大きめの四角形や線でざっくり作ってから細かく整えると楽です。
            """;

    public HelpDialog(MainFrame owner) {

        super(owner, "使い方", true);

        setLayout(new BorderLayout(8, 8));
        setSize(new Dimension(680, 620));
        setLocationRelativeTo(owner);

        JTextArea textArea = new JTextArea(HELP_TEXT);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textArea.setCaretPosition(0);

        add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton("閉じる");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }
}
