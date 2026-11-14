/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.seo.modules.robots.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.portal.service.template.HtmlAttributesMethod;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModelEx;

/**
 * Renders the robots.txt panel with HTML auto-escaping and the back office macros (LUT-33153), as SEORobotsPanel builds
 * its model: the robots.txt and sitemap URLs are written as text and the preview URL goes through safeUrl. The core
 * macros are read from the exploded webapp (lutece:exploded).
 */
public class RobotsPanelRenderTest
{
    private static final String TEMPLATES_ROOT = "webapp/WEB-INF/templates";
    private static final String CORE_TEMPLATES_ROOT = "target/lutece/WEB-INF/templates";
    private static final String COMMONS = "commons_bs5_tabler.html";
    private static final String TEMPLATE_PANEL = "admin/plugins/seo/modules/robots/panel/robots_panel.html";
    private static final String SERVER = "http://a\"b<c>";
    private static final String SERVER_ESCAPED = "http://a&quot;b&lt;c&gt;";

    /**
     * Renders a template of the module with the back office macros auto-included.
     *
     * @param strTemplate
     *            the template
     * @param model
     *            the model
     * @return the HTML
     * @throws IOException
     *             if a template cannot be read
     * @throws TemplateException
     *             if the rendering fails
     */
    private static String render( String strTemplate, Map<String, Object> model ) throws IOException, TemplateException
    {
        assumeTrue( new File( CORE_TEMPLATES_ROOT, COMMONS ).exists( ), "core templates not exploded under " + CORE_TEMPLATES_ROOT );
        Configuration cfg = new Configuration( Configuration.VERSION_2_3_28 );
        // as AbstractFreeMarkerTemplateService configures the engine
        cfg.setNumberFormat( "0.######" );
        cfg.setLocalizedLookup( false );
        cfg.setTemplateLoader( new MultiTemplateLoader( new TemplateLoader [ ] {
                new FileTemplateLoader( new File( TEMPLATES_ROOT ) ), new FileTemplateLoader( new File( CORE_TEMPLATES_ROOT ) )
        } ) );
        cfg.addAutoInclude( COMMONS );
        cfg.setOutputFormat( HTMLOutputFormat.INSTANCE );
        cfg.setAutoEscapingPolicy( Configuration.ENABLE_IF_DEFAULT_AUTO_ESCAPING_POLICY );
        cfg.setSharedVariable( "i18n", (TemplateMethodModelEx) args -> String.valueOf( args.get( 0 ) ) );
        cfg.setSharedVariable( "dskey", (TemplateMethodModelEx) args -> "DS" );
        cfg.setSharedVariable( "htmlAttributes", new HtmlAttributesMethod( true ) );
        StringWriter writer = new StringWriter( );
        cfg.getTemplate( strTemplate ).process( model, writer );

        return writer.toString( );
    }

    /**
     * The model of the panel, as SEORobotsPanel builds it.
     *
     * @param strServer
     *            the scheme, name and port of the server
     * @return the model
     */
    private static Map<String, Object> panelModel( String strServer )
    {
        Map<String, Object> model = new HashMap<>( );
        model.put( "robots_url", strServer + "/robots.txt" );
        model.put( "dns", strServer );
        model.put( "robots_preview", strServer + "/robots.txt" );
        model.put( "sitemap_url", strServer + "/site/sitemap-seo.xml" );

        return model;
    }

    /**
     * The panel writes the URLs as text and the preview URL as an attribute value.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testPanel( ) throws Exception
    {
        String strHtml = render( TEMPLATE_PANEL, panelModel( SERVER ) );

        assertTrue( strHtml.contains( ">" + SERVER_ESCAPED + "/robots.txt</span>" ), strHtml );
        assertTrue( strHtml.contains( "<iframe src=\"" + SERVER_ESCAPED + "/robots.txt\"" ), strHtml );
        assertTrue( strHtml.contains( " : " + SERVER_ESCAPED + "/site/sitemap-seo.xml</span>" ), strHtml );
        assertFalse( strHtml.contains( "<c>" ), strHtml );
        // the panel title follows the h1 of the SEO page
        assertTrue( strHtml.contains( "<h2 class=\"box-title\">#i18n{module.seo.robots.manage_robots.title}</h2>" ), strHtml );
    }

    /**
     * A preview URL whose scheme is not a web one is not written in the iframe.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testPreviewUrlIsChecked( ) throws Exception
    {
        String strHtml = render( TEMPLATE_PANEL, panelModel( "javascript:void(0)//" ) );

        assertTrue( strHtml.contains( "<iframe src=\"#\"" ), strHtml );
    }
}
