/*
 * Copyright (c) 2002-2012, Mairie de Paris
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
package fr.paris.lutece.plugins.seo.modules.robots.web.panel;

import fr.paris.lutece.plugins.seo.service.SEODataKeys;
import fr.paris.lutece.plugins.seo.web.panel.SEOAbstractPanel;
import fr.paris.lutece.plugins.seo.web.panel.SEOPanel;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.util.html.HtmlTemplate;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;




/**
 * Sitemap Panel
 */
public class SEORobotsPanel extends SEOAbstractPanel implements SEOPanel
{
    private static final String TEMPLATE_CONTENT = "/admin/plugins/seo/modules/robots/panel/robots_panel.html";
    private static final String PROPERTY_TITlE = "module.seo.panel.robots.title";
    
    private static final String MARK_ROBOTS_URL = "robots_url";
    private static final String MARK_DNS = "dns";
    private static final String MARK_ROBOTS_PREVIEW = "robots_preview";    
    
    private static final String ROBOTS = "/robots.txt";    
    private static final int ORDER = 4;    
    private static final int PORT_NUMBER_HTTP = 80;    
    

    /**
     * {@inheritDoc }
     */
    @Override
    public String getTitle(  )
    {
        return I18nService.getLocalizedString( PROPERTY_TITlE, getLocale(  ) );
    }

    /**
     * {@inheritDoc }
     */    
    @Override
    public String getContent(   )
    {
        Map<String, Object> model = new HashMap<String, Object>(  );
                
                        
        String strDNS = getRequest().getScheme(  ) + "://" + getRequest().getServerName(  );
        int nPort = getRequest().getServerPort(  );

        if ( nPort != PORT_NUMBER_HTTP )
        {
            strDNS += ( ":" + nPort );
        }

        if ( strDNS.endsWith( "/" ) )
        {
            strDNS += "/";
        }
        
        model.put( MARK_ROBOTS_URL, strDNS + ROBOTS );
        model.put( MARK_DNS, strDNS );
        // URL de test, à supprimer
        strDNS = "http://fr.lutece.paris.fr";
        model.put( MARK_ROBOTS_PREVIEW, strDNS + ROBOTS );
        

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CONTENT, getLocale(  ), model );

        return template.getHtml(  );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int getOrder(  )
    {
        return ORDER;
    }
}
