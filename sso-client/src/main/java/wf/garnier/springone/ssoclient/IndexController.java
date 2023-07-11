package wf.garnier.springone.ssoclient;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class IndexController {

    private final JwtDecoder decoder;

    public IndexController(JwtDecoder decoder) {
        this.decoder = decoder;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String index(@AuthenticationPrincipal OidcUser user, Model model) {
        var idToken = user.getIdToken();
        var decoded = decoder.decode(idToken.getTokenValue());
        model.addAttribute("headers", decoded.getHeaders());
        model.addAttribute("claims", user.getClaims());
        model.addAttribute("id_token", idToken.getTokenValue());
        return "index";
    }

    @GetMapping("/logged-out")
    public String loggedOut() {
        return "logged-out";
    }

}
