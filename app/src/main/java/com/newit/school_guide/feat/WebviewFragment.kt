package com.newit.school_guide.feat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.newit.school_guide.R
import com.newit.school_guide.core.base.BaseFragment
import com.newit.school_guide.databinding.FragmentWebviewBinding

class WebviewFragment : BaseFragment() {

    var privatePolicy = """プライバシーポリシー

学校法人実践女子学園　個人情報保護の基本方針

本学園は、個人の基本的人権並びに個人情報の保護の重要性を十分認識し、教育研究機関としての社会的責任、公共的役割を自覚し、個人情報の取扱いに伴う、個人の権利、利益及びプライバシー侵害の防止に関し、必要な措置を講じることとし、以下の方針を定め、これを実行いたします。

個人情報に関する法令等の遵守 
「個人情報の保護に関する法律」、文部科学省公示「学校における生徒等に関する個人情報の適正な取扱を確保するために事業者が講ずべき措置に関する指針」その他の関連法令等を遵守します。
個人情報の取得
利用目的を明示して、公正妥当な手段により取得します。
個人情報の利用
明示した業務遂行上必要な利用目的の範囲内で取扱い、原則本人の同意なしに第三者への提供は行いません。
個人情報の管理 
個人情報の安全性及び信頼性を確保するために、個人情報への不正な接触並びにその漏えい、滅失、毀損及び改ざん等の危険防止に関し、また個人情報の最新又は最適の状態に保つよう維持管理するため、必要かつ適切な管理措置を講じます。
必要に応じ、明示した利用範囲内で個人データの取扱いを外部へ業務委託する場合、その委託先に対しても、個人情報の安全性が確保されるよう、必要かつ適切な監督を行います。
個人情報の開示・訂正・削除等 
本人からの開示・訂正・削除等の請求に対して、教育活動に与える影響を勘案し、速やかに妥当な範囲内において対応します。
組織・体制 
個人情報保護の重要性を認識し、「実践女子学園個人情報の保護に関する規程」を制定実施し、これらを維持し、継続的に改善及び向上につとめます。
学園内における個人情報の適正な取扱いを徹底するため、教職員に対して継続的な研修を実施する等の方策を講じます。
実践女子学園個人情報の保護に関する規程 [PDF:152KB]
個人情報の取扱について

個人情報の利用目的について
　本学で取り扱う個人情報は、教育研究及び学生支援等に必要な以下の業務を遂行するために利用します。利用目的の達成に必要な範囲を超えて、個人情報を取り扱うことはありません。
なお、個人情報の収集は、以下の業務の範囲から収集目的を定めて、適法かつ公正な手段によって収集し、不正な手段によっては情報を収集しません。また、自明の場合を除き、その利用目的を明らかにします。
利用業務内容 
(1) 学生（科目等履修生、高大連携科目履修生徒、協定留学生、特別聴講生等を含む）
・ 修学関係
履修相談・修学指導。学業奨励。研究活動支援。履修登録。教職等諸資格課程登録（申請業務含む）。授業・試験運営。成績処理。単位認定。卒業（修了）判定。諸資格判定。学位記授与。単位互換協定による国内留学。海外留学。短期海外留学。国際交流。留学生在留手続き。
・ 学籍関係
休学・復学・退学手続き。転学部・転学科手続き。再入学、卒業延期（修了延期）手続き。
・ 学生生活
学生生活全般に関わる指導・助言。学生証発行。福利厚生施設・行政機関の紹介。奨学生選考。奨学金交付・償還（申請手続き含む）。定期健康診断。日常的な健康相談。メンタルヘルス相談。課外活動支援。弔慰、震災見舞、保険案内・加入・請求手続き。
・進路関係
就職支援キャリア（進路）形成支援。求職登録。就職斡旋。インターンシップ手続き。資格取得のための課外講座運営。就職支援講座手続き。進路調査。
・社会連携関係
地域連携・産学連携等に関わる社会連携・プロジェクト・ボランティア・イベント活動に関する諸手続き。
・施設利用
図書館及び図書・資料の利用環境の提供。コンピュータ等情報環境の提供。視聴覚施設、教室、体育施設、合宿研修施設等。
・その他
IR（Institutional　Research）業務。運営・管理。各種連絡・通知。諸証明書発行。用具・備品等の貸与。学則による処分。大学広報活動。書類・刊行物・物品等の発送業務。その他諸通知等に関する業務。
(2) 保証人・保護者等
学生の修学指導等に必要な連絡。各種送付物（学業成績通知書、学費納付書、大学行事案内保険案内等） の発送。大学関係諸団体（「後援会」等）の要請によるご父母宛。送付物の発送業務。後援会運営・管理。キャリア（進路）形成支援。就職支援。
(3) 受講者
公開・生涯学習・リカレント講座案内、講座受講料納入通知、講座受講管理に関する業務。
(4) 卒業生・同窓生
卒業・成績・在籍等の証明に関する業務。IR（Institutional　Research）業務。(社)実践桜会運営・管理。在学生・卒業生就職支援。在学生キャリア（進路）形成支援。卒業生相互の親睦。進路調査。就職支援。就職斡旋。貸与奨学金に関する業務。各種送付物（学報等広報誌、年賀状、大学案内、催し物案内、募金依頼等）。実習所の施設利用に関連する業務。
(5) 入学志願者
相談会等運営業務。入学志願者に対する選抜試験運営。入学手続（編入学、再入学を含む）。入試広報分析業務。IR（Institutional　Research）業務。入学前教育課題送付業務。保険案内。
(6) 資料請求者
資料等発送、諸行事案内、請求通知、入試広報分析業務。IR（Institutional　Research）業務。
(7) 教職員（専任・非常勤・外部講師・業務委託会社・派遣社員・ＴＡ・ＲＡ・臨時補助員・退職者を含む）
人事、給与、労務、厚生、採用、保健、保険、財務、庶務および組織運営に関する業務。教員資格審査手続き。研究業績管理。研究費管理。書類・刊行物・物品等の発送業務。諸通知に関する業務。在学生・卒業生のキャリア（進路）形成支援。在学生・卒業生の就職支援業務。コンピュータ等情報環境の提供。実習所の施設利用に関連する業務。後援会顧問・相談役への資料送付業務。
(8) 企業・団体（取引企業、各種届出団体、行政庁関係、関係法人、進路企業等）
契約、納入・請求管理、支払い、諸通知に関する業務。教職等諸資格課程申請手続き。奨学金手続き。在学生就職支援助勢。キャリア（進路）形成支援。進路調査。就職支援。就職斡旋。
(9) 卒業生保護者
後援会会報の発送。
個人情報の第三者提供について
　本学では、教育研究の観点から学生の個人情報を以下のとおり第三者に提供することがあります。なお、これ以外に提供の必要性が生じたときには、都度、本人への意思確認の手続きをとります。

(1) 保証人（ご父母等）への学業成績等の通知について
本学では、保証人（ご父母等）と連携した学修支援を行うことが教育上有効な取組みであると考え、保証人（ご父母等）への学修状況（履修科目、成績、出席状況、GPA 等）、各部署への相談内容の開示を行うことがあります。
具体的な例として、保証人の皆様（ご父母等）へ、以下のスケジュールで成績通知表を郵送しております。
・前期：９月上旬に、大学・短期大学部の全学年の保証人様を対象に送付
・後期：３月中旬に、大学１～３年生、短期大学部１年生の保証人様を対象に送付
＊本学後援会の総会開催日に学科別個人面談を希望された場合は個人面談の資料としても活用されます。
保証人様への成績送付は、個人情報の第三者提供にあたるため、学生本人の同意が必要となります。学生本人が不同意の手続きを行った場合には、成績は送付せず、その旨をお知らせいたします。
個人情報の保護に関する法律においては、本人の同意を得ずに個人情報を第三者に提供することが禁じられております。しかし、本学では、保証人（ご父母等）へ学修状況や成果についての報告をする責任があると考えており、今後も学修状況の開示を行う予定です。ただし、開示に際しては、学生本人にその旨を通知し同意を得るものとします。 また保証人（ご父母等）への開示は学生本人との関係を確認した上で行います。
　学生本人への成績開示について
教務課が管理する成績の開示は、成績通知、学生支援システム（J-TAS)による閲覧、および単位成績証明書の発行をもって行います。

(2)（社）実践桜会との共同利用について
本学では、個人情報を卒業後も卒業生情報として管理する他、機関誌の送付など卒業生の親睦や互助に資することを利用目的の範囲内として、大学の同窓会組織である（社）実践桜会と共同で利用させていただきます。
なお、（社）実践桜会は、本学と同様、個人情報保護に必要な安全管理措置を講じております。また、提供していただいた個人情報を共同利用の範囲を超えて第三者に提供することはありません。

共同利用目的
機関誌、桜会の案内の送付など卒業生の親睦や互助に資するため

共同利用するデータ項目
氏名、卒期、学部学科、住所、電話番号、勤務先
お問い合わせ先
学校法人　実践女子学園　総務部 
電話番号　042-585-8800　（ダイヤルイン）"""

    private val viewModel: WebviewViewModel by viewModels()

    private var _binding: FragmentWebviewBinding? = null
    private val binding get() = _binding!!

    override fun getLayoutId(): Int {
        return R.layout.fragment_webview
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWebviewBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun viewReadyToUse() {
        binding.toolbar.icLeft.setImageResource(R.drawable.ic_close)
        binding.toolbar.tvTitle.setText("")
        binding.toolbar.icLeft.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                hideLoading()
            }
        }
        arguments?.getString("url")?.let {
            binding.webview.loadUrl(it)
            showLoading()
        }
        if (arguments?.getBoolean("isPolicy") == true) {
//            webview.loadData(privatePolicy, "text/html; charset=utf-8", "UTF-8")
            binding.webview.visibility = View.GONE
            binding.scrPolicy.visibility = View.VISIBLE
            binding.tvContent.text = privatePolicy
        }
        arguments?.getString("title")?.let {
            binding.toolbar.tvTitle.text = it
        }
        arguments?.getString("title_infomation")?.let {
            binding.tvTitle2.text = it
            binding.tvTitle2.visibility = View.VISIBLE
//            (activity as? BaseActivity)?.resetBadgeCounterOfPushMessages()
//            (activity as? MainActivity)?.removeBadge()
        }
        arguments?.getInt("id_infomation")?.let {
            var unread = arguments?.getInt("unread")
            if (unread == 1) {
                viewModel.postReadInfo(it)
            }
        }
    }
}