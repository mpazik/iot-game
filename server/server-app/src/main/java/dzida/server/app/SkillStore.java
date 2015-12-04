package dzida.server.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Maps;
import dzida.server.core.skill.Skill;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.Map;

public class SkillStore {
    private static final URI staticServerAddress = URI.create("http://localhost:8080");

    Map<Integer, Skill> loadSkills() {


        Map<Integer, SkillBean> skillBeans = getSkillsClient()
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<Map<Integer, SkillBean>>() {
                });

        return Maps.transformValues(skillBeans, bean -> new Skill(
                bean.getId(),
                bean.getType(),
                bean.getDamage(),
                bean.getRange(),
                bean.getCooldown(),
                bean.getTarget())
        );
    }

    private WebTarget getSkillsClient() {
        return ClientBuilder.newClient()
                .target(staticServerAddress)
                .path("assets").path("skills.json");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SkillBean {
        private int id;
        private int type;
        private double damage;
        private double range;
        private int cooldown;
        private int target;

        public int getId() {
            return id;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public double getDamage() {
            return damage;
        }

        public void setDamage(double damage) {
            this.damage = damage;
        }

        public double getRange() {
            return range;
        }

        public void setRange(double range) {
            this.range = range;
        }

        public int getCooldown() {
            return cooldown;
        }

        public void setCooldown(int cooldown) {
            this.cooldown = cooldown;
        }

        public int getTarget() {
            return target;
        }

        public void setTarget(int target) {
            this.target = target;
        }
    }
}
