package com.news.scanner.news;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.news.scanner.dto.Link;
import com.news.scanner.repositories.NewsRepository;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class VnuNewsScanner extends NewsScanner {

  public static final String IS = "https://www.is.vnu.edu.vn/";
  public static final String VNU = "https://vnu.edu.vn/";
  public static final String UEB = "https://ueb.edu.vn/";
  public static final String PRESS = "https://press.vnu.edu.vn/";
  public static final String HUS = "https://hus.vnu.edu.vn/";
  public static final String EDC = "https://education.vnu.edu.vn/";
  public static final String YSIP = "https://ysip.vnu.edu.vn/";
  public static final String VJU = "https://vju.ac.vn/";
  public static final String ITI = "https://iti.vnu.edu.vn/vi/";
  public static final String HSB = "https://hsb.edu.vn/";
  public static final String TNTI = "https://tnti.vnu.edu.vn/";
  public static final String ULIS = "https://ulis.vnu.edu.vn/";
  public static final String UMP = "https://ump.vnu.edu.vn/";
  public static final String ALUMNI = "https://alumni.vnu.edu.vn/";
  public static final String LAW = "https://law.vnu.edu.vn/";
  public static final String SIS = "https://sis.vnu.edu.vn/";
  public static final String CEA = "https://cea.vnu.edu.vn/";
  public static final String HDC = "https://hdc.vnu.edu.vn/";
  public static final String CMC = "https://cmc.vnu.edu.vn/";
  public static final String USSH = "https://ussh.vnu.edu.vn/vi/";
  public static final String IMBT = "https://imbt.vnu.edu.vn/";
  public static final String INFEQA = "https://infeqa.vnu.edu.vn/";
  public static final String CET = "https://cet.vnu.edu.vn";
  public static final String IDIDES = "https://ivides.vnu.edu.vn/ ";
  public static final String CSS = "https://css.vnu.edu.vn/";
  public static final String IFI = "http://www.ifi.vnu.edu.vn/";
  public static final String DATA_PARENT = "data-parent";

  ChromeDriver chromeDriver;
  NewsRepository newsRepository;

  @Override
  String getBaseUrl() {
    return "https://vnu.edu.vn/";
  }

  @Override
  String getDomain() {
    return "vnu.edu";
  }

  @Override
  List<String> getSubDomainCategories(String subDomain) {
    String raw = "đại học quốc gia";
    Map<String, List<String>> categories = new HashMap<>();
    categories.put(IS, List.of(raw, "quốc tế"));
    categories.put(VNU, List.of(raw));
    categories.put(UEB, List.of(raw, "kinh tế"));
    categories.put(PRESS, List.of(raw, "nhà xuất bản"));
    categories.put(HUS, List.of(raw, "khoa học", "tự nhiên"));
    categories.put(EDC, List.of(raw, "giáo dục"));
    categories.put(YSIP, List.of(raw, "đào tạo", "thạc sĩ", "tiến sĩ"));
    categories.put(VJU, List.of(raw, "việt nhật"));
    categories.put(ITI, List.of(raw, "công nghệ thông tin"));
    categories.put(HSB, List.of(raw, "quản trị", "kinh doanh"));
    categories.put(TNTI, List.of(raw, "phật giáo"));
    categories.put(ULIS, List.of(raw, "ngoại ngữ"));
    categories.put(UMP, List.of(raw, "y dược"));
    categories.put(ALUMNI, List.of(raw, "cực sinh viên"));
    categories.put(LAW, List.of(raw, "luật"));
    categories.put(SIS, List.of(raw, "khoa học liên nghành", "nghệ thuật"));
    categories.put(CEA, List.of(raw, "kiểm định", "chất lượng", "giáo dục"));
    categories.put(HDC, List.of(raw, "trung tâm", "dự báo", "phát triển nguồn lực"));
    categories.put(CMC, List.of(raw, "trung tâm", "qunar lý", "đô thị"));
    categories.put(USSH, List.of(raw, "khoa học", "xã hội", "nhân văn"));
    categories.put(IMBT, List.of(raw, "vi sinh", "công nghệ sinh học"));
    categories.put(INFEQA, List.of(raw, "đảm bảo chất lượng giáo dục"));
    categories.put(CET, List.of(raw, "khảo thí"));
    categories.put(IDIDES, List.of(raw, "việt nam", "khoa học", "phát triển"));
    categories.put(CSS, List.of(raw, "hỗ trợ sinh viên"));
    categories.put(IFI, List.of(raw, "pháp ngữ"));
    return categories.get(subDomain);
  }

    @Override
    List<String> getSubDomain() {
       // return List.of(IS, VNU, UEB, PRESS, HUS, EDC, YSIP, VJU, ITI, HSB, TNTI, ULIS, UMP, ALUMNI, LAW, SIS, CEA, HDC, CMC, USSH, IMBT, INFEQA, CET, IDIDES, CSS, IFI);
        return List.of(EDC);
    }

    @Override
    protected List<String> getNoneCrawlLinks() {
        return List.of("https://vnu.edu.vn/home/?C151/N26741",
                "https://vnu.edu.vn/home/?C151/N26846",
                "https://vnu.edu.vn/home/?C151/N26845");
    }

  public void scanByUrl(Link rootLink) {
    Optional<Document> documentOptional = getDocument(rootLink.getUrl(), chromeDriver);

    documentOptional.ifPresent(document -> {
      Set<String> scanUrlSet = chromeDriver.findElements(By.tagName(NewsScanner.A_TAG))
          .stream()
          .filter(aTag -> !ObjectUtils.isEmpty(aTag.getAttribute(NewsScanner.HREF)))
          .filter(aTag -> !aTag.getAttribute(NewsScanner.HREF).contains("/en"))
          .filter(aTag -> aTag.getAttribute(NewsScanner.HREF).startsWith(rootLink.getDomain()))
          .filter(
              aTag -> !aTag.getAttribute(NewsScanner.HREF).contains("?fb")) // IS domain trash link
          .filter(aTag -> {
                if (ObjectUtils.isEmpty(aTag.getAttribute(DATA_PARENT))) {
                    return true;
                }

                return !aTag.getAttribute(DATA_PARENT).startsWith("#");
              }
          )
          .filter(aTag -> !aTag.getAttribute(NewsScanner.HREF).contains("collapse"))
          .filter(aTag -> !aTag.getAttribute(NewsScanner.HREF).contains("#"))
          .filter(aTag -> !aTag.getAttribute(NewsScanner.HREF).contains("/login"))
          .filter(aTag -> !aTag.getAttribute(NewsScanner.HREF).contains("/register"))
          .map(aTag -> aTag.getAttribute(NewsScanner.HREF).strip()
              .replace("/respond", "/"))
          .collect(Collectors.toSet());

            scanUrlSet.forEach(scanUrl ->addDocumentCollectionForCrawl(scanUrl, rootLink.getDomain()));
            saveNews(document, rootLink, newsRepository);
        });
    }

  @Override
  List<String> findPageCategories(Document document, String domain) {
    return switch (domain) {
      case IS -> findISCategories(document, domain);
      case VNU -> findVNUCategories(document, domain);
      case UEB -> findUEBCategories(document, domain);
      case PRESS -> findPRESSCategories(document, domain);
      case HUS -> findHUSCategories(document, domain);
      case EDC -> findEDCCategories(document, domain);
      case YSIP -> findYSIPCategories(document, domain);
      case VJU -> findVJUCategories(document, domain);
      case ITI -> findITICategories(document, domain);
      case HSB -> findHSBCategories(document, domain);
      case TNTI -> findTNTICategories(document, domain);
      case ULIS -> findULISCategories(document, domain);
      case UMP -> findUMPCategories(document, domain);
      case ALUMNI -> findALUMNICategories(document, domain);
      case LAW -> findLAWCategories(document, domain);
      case SIS -> findSISCategories(document, domain);
      case CEA -> findCEACategories(document, domain);
      case HDC -> findHDCCategories(document, domain);
      case CMC -> findCMCCategories(document, domain);
      case USSH -> findUSSHCategories(document, domain);
      case IMBT -> findIMBTCategories(document, domain);
      case INFEQA -> findINFEQACategories(document, domain);
      case CET -> findCETCategories(document, domain);
      case IDIDES -> findIDIDESCategories(document, domain);
      case CSS -> findCSSCategories(document, domain);
      case IFI -> findIFICategories(document, domain);
      default -> Collections.emptyList();
    };
  }

  @Override
  String findPageContent(Document document, String domain) {
    return switch (domain) {
      case IS -> findISContent(document, domain);
      case VNU -> findVNUContent(document, domain);
      case UEB -> findUEBContent(document, domain);
      case PRESS -> findPRESSContent(document, domain);
      case HUS -> findHUSContent(document, domain);
      case EDC -> findEDCContent(document, domain);
      case YSIP -> findYSIPContent(document, domain);
      case VJU -> findVJUContent(document, domain);
      case ITI -> findITIContent(document, domain);
      case HSB -> findHSBContent(document, domain);
      case TNTI -> findTNTIContent(document, domain);
      case ULIS -> findULISContent(document, domain);
      case UMP -> findUMPContent(document, domain);
      case ALUMNI -> findALUMNIContent(document, domain);
      case LAW -> findLAWContent(document, domain);
      case SIS -> findSISContent(document, domain);
      case CEA -> findCEAContent(document, domain);
      case HDC -> findHDCContent(document, domain);
      case CMC -> findCMCContent(document, domain);
      case USSH -> findUSSHContent(document, domain);
      case IMBT -> findIMBTContent(document, domain);
      case INFEQA -> findINFEQAContent(document, domain);
      case CET -> findCETContent(document, domain);
      case IDIDES -> findIDIDESContent(document, domain);
      case CSS -> findCSSContent(document, domain);
      case IFI -> findIFIContent(document, domain);
      default -> "";
    };
  }

  private String findVNUContent(Document document, String domain) {
    if (!ObjectUtils.isEmpty(document.select(".catcontent"))) {
      return document.select(".catcontent").first().text();
    }

    if (!ObjectUtils.isEmpty(document.select(".news-show"))) {
      return document.select(".news-show").first().text();
    }

    if (!ObjectUtils.isEmpty(document.select(".conten_cate"))) {
      return document.select(".conten_cate").first().text();
    }
    return "";
  }

  private List<String> findVNUCategories(Document document, String domain) {
    Elements titles = null;

    if (!ObjectUtils.isEmpty(document.select("td.tdlinktitle a"))) {
      titles = document.select("td.tdlinktitle a");
      return titles.stream()
          .filter(Element::hasText)
          .map(Element::text)
          .collect(Collectors.toList());
    }

    if (!ObjectUtils.isEmpty(document.select("div.title-cate a"))) {
      titles = document.select("div.title-cate a");
      return titles.stream()
          .filter(Element::hasText)
          .map(Element::text)
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  private String findISContent(Document document, String domain) {
    Elements content = null;
    if (!ObjectUtils.isEmpty(document.select("#tin-cap-nhat-homepage"))) {
      content = document.select("#tin-cap-nhat-homepage");
      return content.first().text();
    }

    if (!ObjectUtils.isEmpty(document.select(".entry-content"))) {
      content = document.select(".entry-content");
      return content.first().text();
    }

    if (!ObjectUtils.isEmpty(document.select(".main-content"))) {
      content = document.select(".main-content");
      return content.first().text();
    }

    return "";
  }

  private List<String> findISCategories(Document document, String domain) {
    Elements titles = null;
    if (!ObjectUtils.isEmpty(document.select("a.collapsed"))) {
      titles = document.select("a.collapsed");
      return titles.stream()
          .filter(Element::hasText)
          .map(Element::text)
          .collect(Collectors.toList());
    }

    if (!ObjectUtils.isEmpty(document.select("center h3"))) {
      titles = document.select("center h3");
      return titles.stream()
          .filter(Element::hasText)
          .map(Element::text)
          .collect(Collectors.toList());
    }

    if (!ObjectUtils.isEmpty(document.select("div.post-detail h1"))) {
      titles = document.select("div.post-detail h1");
      return titles.stream()
          .filter(Element::hasText)
          .map(Element::text)
          .collect(Collectors.toList());
    }
    if (!ObjectUtils.isEmpty(document.select("div.teams-list h2"))) {
      titles = document.select("div.teams-list h2");
      return titles.stream()
          .filter(Element::hasText)
          .map(Element::text)
          .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }

    private String findUEBContent(Document document, String domain) {
        Elements content = null;

        if(ObjectUtils.isEmpty(findUEBCategories(document, domain))) {
            return "";
        }

        if(!ObjectUtils.isEmpty(document.select( "div.about-news-detail div.container"))){
            content = document.select("div.about-news-detail div.container");
            return content.first().text();
        }

        if(!ObjectUtils.isEmpty(document.select( "div.section.about-news-second div.container"))){
            content = document.select("div.section.about-news-second div.container");
            return content.first().text();
        }


        return "";
    }

    private List<String> findUEBCategories(Document document, String domain) {
        Elements titles = null;

        if(!ObjectUtils.isEmpty(document.select( "li.breadcrumb-item.uebnavi a"))){
            titles = document.select("li.breadcrumb-item.uebnavi a");
            return titles.stream()
                    .filter(Element::hasText)
                    .map(Element::text)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

  private String findPRESSContent(Document document, String domain) {
    Elements content = null;

    if(!ObjectUtils.isEmpty(document.select( "div.page-body"))){
      content = document.select("div.page-body");
      return content.first().text();
    }

    return "";
  }


  private List<String> findPRESSCategories(Document document, String domain) {
    Elements titles = null;
    if(!ObjectUtils.isEmpty(document.select( "div.page-title"))){
      titles = document.select("div.page-title");
     return titles.stream()
          .filter(Element::hasText)
          .map(Element::text)
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  private String findHUSContent(Document document, String domain) {
    Elements content = null;

    if(!ObjectUtils.isEmpty(document.select( "div.news-details"))){
      content = document.select("div.news-details");
      return content.first().text();
    }

    if(!ObjectUtils.isEmpty(document.select( "div.single-blog-content"))){
      content = document.select("div.single-blog-content");
      return content.first().text();
    }

    return "";
  }

  private List<String> findHUSCategories(Document document, String domain) {
    Elements titles = null;
    if(!ObjectUtils.isEmpty(document.select( "ol.breadcrumb"))){
      titles = document.select("ol.breadcrumb");
      return titles.stream()
          .filter(Element::hasText)
          .map(Element::text)
          .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }

  private String findEDCContent(Document document, String domain) {
    Elements content = null;

    if(!ObjectUtils.isEmpty(document.select( "div.panel-body"))){
      content = document.select("div.panel-body");
      return content.first().text();
    }

    if(!ObjectUtils.isEmpty(document.select( "div.container-fluid.text-center div.col-sm-8.text-left"))){
      content = document.select("div.container-fluid.text-center div.col-sm-8.text-left");
      return content.first().text();
    }

    return "";
  }

  private List<String> findEDCCategories(Document document, String domain) {
    Elements titles = null;
    if(!ObjectUtils.isEmpty(document.select( "div.container-fluid div.col-sm-8 h1"))){
      titles = document.select("div.container-fluid div.col-sm-8 h1");
      return titles.stream()
              .filter(Element::hasText)
              .map(Element::text)
              .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  private String findVJUContent(Document document, String domain) {
    return "";
  }

  private String findYSIPContent(Document document, String domain) {
    return "";
  }

  private String findITIContent(Document document, String domain) {
    return "";
  }

  private String findHSBContent(Document document, String domain) {
    return "";
  }

  private String findTNTIContent(Document document, String domain) {
    return "";
  }

  private String findULISContent(Document document, String domain) {
    return "";
  }

  private String findUMPContent(Document document, String domain) {
    return "";
  }

  private String findALUMNIContent(Document document, String domain) {
    return "";
  }

  private String findLAWContent(Document document, String domain) {
    return "";
  }

  private String findSISContent(Document document, String domain) {
    return "";
  }

  private String findCEAContent(Document document, String domain) {
    return "";
  }

  private String findHDCContent(Document document, String domain) {
    return "";
  }

  private String findCMCContent(Document document, String domain) {
    return "";
  }

  private String findUSSHContent(Document document, String domain) {
    return "";
  }

  private String findIMBTContent(Document document, String domain) {
    return "";
  }

  private String findINFEQAContent(Document document, String domain) {
    return "";
  }

  private String findCETContent(Document document, String domain) {
    return "";
  }

  private String findIDIDESContent(Document document, String domain) {
    return "";
  }

  private String findCSSContent(Document document, String domain) {
    return "";
  }

  private String findIFIContent(Document document, String domain) {
    return "";
  }


  private List<String> findIFICategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findCSSCategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findIDIDESCategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findCETCategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findINFEQACategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findIMBTCategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findUSSHCategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findCMCCategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findHDCCategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findCEACategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findSISCategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findLAWCategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findALUMNICategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findUMPCategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findULISCategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findTNTICategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findHSBCategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findITICategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findVJUCategories(Document document, String domain) {
    return Collections.emptyList();
  }

  private List<String> findYSIPCategories(Document document, String domain) {
    return Collections.emptyList();
  }


}
