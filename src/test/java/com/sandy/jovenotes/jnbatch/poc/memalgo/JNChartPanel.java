package com.sandy.jovenotes.jnbatch.poc.memalgo;

import java.awt.BorderLayout ;
import java.awt.Color ;
import java.awt.Font ;
import java.util.Date ;
import java.util.Hashtable ;
import java.util.Map ;

import javax.swing.JPanel ;
import javax.swing.SwingUtilities ;

import org.apache.log4j.Logger ;
import org.jfree.chart.ChartFactory ;
import org.jfree.chart.ChartPanel ;
import org.jfree.chart.JFreeChart ;
import org.jfree.chart.annotations.XYTextAnnotation ;
import org.jfree.chart.axis.ValueAxis ;
import org.jfree.chart.plot.XYPlot ;
import org.jfree.chart.title.LegendTitle ;
import org.jfree.data.time.Second ;
import org.jfree.data.time.TimeSeries ;
import org.jfree.data.time.TimeSeriesCollection ;

import com.sandy.jovenotes.jnbatch.job.preparedness.algo.RetentionAlgorithmListener ;

@SuppressWarnings( "serial" )
public class JNChartPanel extends JPanel 
    implements RetentionAlgorithmListener {
    
    static final Logger log = Logger.getLogger( JNChartPanel.class ) ;
    
    private static final String FONT_NAME = "Helvetica" ;
    
    public static final Font STD_FONT          = new Font( FONT_NAME, Font.PLAIN, 11 ) ;
    public static final Font CHART_AXIS_FONT   = new Font( FONT_NAME, Font.PLAIN, 10 ) ;
    public static final Font CHART_LEGEND_FONT = new Font( FONT_NAME, Font.PLAIN, 12 ) ;

    private static final Font AXIS_FONT   = CHART_AXIS_FONT ;
    private static final Font LEGEND_FONT = CHART_LEGEND_FONT ;
    
    private TimeSeriesCollection    seriesColl = null ;
    private Map<String, TimeSeries> timeSeriesMap = new Hashtable<>() ;
    
    private JFreeChart       chart = null ;
    private XYPlot           plot  = null ;
    private ChartPanel       chartPanel = null ;
    
    public JNChartPanel() {
        
        seriesColl = new TimeSeriesCollection() ;
        createChart() ;
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() ) ;

        chartPanel = new ChartPanel( chart ) ;
        add( chartPanel ) ;
    }
    
    private void createChart() {
        chart = ChartFactory.createTimeSeriesChart( 
                      "Preparedness", 
                      null, 
                      "Preparedness", 
                      seriesColl ) ;
        chart.setBackgroundPaint( Color.BLACK ) ;
        
        configurePlot() ;
        configureAxes() ;
        configureLegends() ;
    }
    
    private void configurePlot() {
        
        plot = ( XYPlot )chart.getPlot() ;
        plot.setBackgroundPaint( Color.BLACK ) ;
        plot.setDomainGridlinePaint( Color.DARK_GRAY ) ;
        plot.setRangeGridlinePaint( Color.DARK_GRAY ) ;
        plot.setRangePannable( true ) ;
        plot.setDomainPannable( true ) ;
    }
    
    private void configureAxes() {
        
        ValueAxis xAxis = plot.getRangeAxis() ;
        ValueAxis yAxis = plot.getDomainAxis() ;
        
        xAxis.setLabelFont( AXIS_FONT ) ;
        yAxis.setLabelFont( AXIS_FONT ) ;
        
        xAxis.setTickLabelFont( AXIS_FONT ) ;
        yAxis.setTickLabelFont( AXIS_FONT ) ;
        
        xAxis.setTickLabelPaint( Color.LIGHT_GRAY.darker() ) ;
        yAxis.setTickLabelPaint( Color.LIGHT_GRAY.darker() ) ;
    }
    
    private void configureLegends() {
        
        LegendTitle legend = chart.getLegend() ;
        legend.setItemFont( LEGEND_FONT ) ;
        legend.setItemPaint( Color.LIGHT_GRAY.darker() ) ;
        legend.setBackgroundPaint( Color.BLACK );
    }
    
    public void addTimeSeriesPoint( String series, Date date, double yValue ) {
        TimeSeries timeSeries = timeSeriesMap.get( series ) ;
        if( timeSeries == null ) {
            timeSeries = new TimeSeries( series ) ;
            seriesColl.addSeries( timeSeries ) ;
            timeSeriesMap.put( series, timeSeries ) ;
        }
        timeSeries.addOrUpdate( new Second( date ), yValue ) ;
    }
    
    public void clear() {
        plot.clearAnnotations() ;
        seriesColl.removeAllSeries() ;
        timeSeriesMap.clear() ;
    }

    @Override
    public void handleRetentionLevelChange( final String series, final Date date, 
                                            final double level ) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                addTimeSeriesPoint( series, date, level ) ;
            }
        } );
    }
    
    public void addAnnotation( final String text, final Date date, 
                               final double level ) {
        
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                XYTextAnnotation a = new XYTextAnnotation( text, date.getTime(), level ) ;
                a.setFont( CHART_LEGEND_FONT ) ;
                a.setPaint( Color.CYAN );
                plot.addAnnotation( a ) ;
            }
        } ) ;
    }
}
