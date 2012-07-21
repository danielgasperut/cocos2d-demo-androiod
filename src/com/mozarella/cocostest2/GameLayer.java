package com.mozarella.cocostest2;

import java.util.ArrayList;
import java.util.Random;

import org.cocos2d.actions.instant.CCCallFuncN;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor4B;

import android.view.MotionEvent;

public class GameLayer extends CCColorLayer {
	
	protected ArrayList<CCSprite> _targets;
	protected ArrayList<CCSprite> _projectiles;
	
	public static CCScene scene()
	{
	    CCScene scene = CCScene.node();
	    CCColorLayer layer = new GameLayer(ccColor4B.ccc4(255, 255, 255, 255));
	 
	    scene.addChild(layer);
	 
	    return scene;
	}
	
	protected GameLayer(ccColor4B color)
	{
		super(color);
	
		_targets = new ArrayList<CCSprite>();
		_projectiles = new ArrayList<CCSprite>();
		
	    CGSize winSize = CCDirector.sharedDirector().displaySize();
	    CCSprite player = CCSprite.sprite("Player.png");
	 
	    player.setPosition(CGPoint.ccp(player.getContentSize().width / 2.0f, winSize.height / 2.0f));
	 
	    addChild(player);

	    
	    this.schedule("gameLogic", 1.0f);
	    this.setIsTouchEnabled(true);
	    this.schedule("update");
	    
	    
	}
	
	public void gameLogic(float dt)
	{
	    addTarget();
	}
	
	protected void addTarget()
	{
	    Random rand = new Random();
	    CCSprite target = CCSprite.sprite("Target.png");
	 
	    // Determine where to spawn the target along the Y axis
	    CGSize winSize = CCDirector.sharedDirector().displaySize();
	    int minY = (int)(target.getContentSize().height / 2.0f);
	    int maxY = (int)(winSize.height - target.getContentSize().height / 2.0f);
	    int rangeY = maxY - minY;
	    int actualY = rand.nextInt(rangeY) + minY;
	 
	    // Create the target slightly off-screen along the right edge,
	    // and along a random position along the Y axis as calculated above
	    target.setPosition(CGPoint.ccp(winSize.width + (target.getContentSize().width / 2.0f), actualY));
	    addChild(target);
	    target.setTag(1);
	    _targets.add(target);
	 
	    // Determine speed of the target
	    int minDuration = 2;
	    int maxDuration = 4;
	    int rangeDuration = maxDuration - minDuration;
	    int actualDuration = rand.nextInt(rangeDuration) + minDuration;
	 
	    // Create the actions
	    CCMoveTo actionMove = CCMoveTo.action(actualDuration, CGPoint.ccp(-target.getContentSize().width / 2.0f, actualY));
	    CCCallFuncN actionMoveDone = CCCallFuncN.action(this, "spriteMoveFinished");
	    CCSequence actions = CCSequence.actions(actionMove, actionMoveDone);
	 
	    target.runAction(actions);

	}
	
	@Override
	public boolean ccTouchesEnded(MotionEvent event)
	{
	    // Choose one of the touches to work with
	    CGPoint location = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));
	 
	    // Set up initial location of projectile
	    CGSize winSize = CCDirector.sharedDirector().displaySize();
	    CCSprite projectile = CCSprite.sprite("Projectile.png");
	 
	    projectile.setPosition(CGPoint.ccp(20, winSize.height / 2.0f));
	 
	    // Determine offset of location to projectile
	    int offX = (int)(location.x - projectile.getPosition().x);
	    int offY = (int)(location.y - projectile.getPosition().y);
	 
	    // Bail out if we are shooting down or backwards
	    if (offX <= 0)
	        return true;
	 
	    // Ok to add now - we've double checked position
	    addChild(projectile);
	    projectile.setTag(2);
	    _projectiles.add(projectile);
	    
	    // Determine where we wish to shoot the projectile to
	    int realX = (int)(winSize.width + (projectile.getContentSize().width / 2.0f));
	    float ratio = (float)offY / (float)offX;
	    int realY = (int)((realX * ratio) + projectile.getPosition().y);
	    CGPoint realDest = CGPoint.ccp(realX, realY);
	 
	    // Determine the length of how far we're shooting
	    int offRealX = (int)(realX - projectile.getPosition().x);
	    int offRealY = (int)(realY - projectile.getPosition().y);
	    float length = (float)Math.sqrt((offRealX * offRealX) + (offRealY * offRealY));
	    float velocity = 480.0f / 1.0f; // 480 pixels / 1 sec
	    float realMoveDuration = length / velocity;
	 
	    // Move projectile to actual endpoint
	    projectile.runAction(CCSequence.actions(
	            CCMoveTo.action(realMoveDuration, realDest),
	            CCCallFuncN.action(this, "spriteMoveFinished")));
	 
	    return true;
	}
	
	
	public void spriteMoveFinished(Object sender)
	{
	    CCSprite sprite = (CCSprite)sender;
	    this.removeChild(sprite, true);
	    
	    if (sprite.getTag() == 1)
	        _targets.remove(sprite);
	    else if (sprite.getTag() == 2)
	        _projectiles.remove(sprite);
	}
	
	public void update(float dt)
	{
	    ArrayList<CCSprite> projectilesToDelete = new ArrayList<CCSprite>();
	 
	    for (CCSprite projectile : _projectiles)
	    {
	        CGRect projectileRect = CGRect.make(projectile.getPosition().x - (projectile.getContentSize().width / 2.0f),
	                                            projectile.getPosition().y - (projectile.getContentSize().height / 2.0f),
	                                            projectile.getContentSize().width,
	                                            projectile.getContentSize().height);
	 
	        ArrayList<CCSprite> targetsToDelete = new ArrayList<CCSprite>();
	 
	        for (CCSprite target : _targets)
	        {
	            CGRect targetRect = CGRect.make(target.getPosition().x - (target.getContentSize().width),
	                                            target.getPosition().y - (target.getContentSize().height),
	                                            target.getContentSize().width,
	                                            target.getContentSize().height);
	 
	            if (intersects(projectileRect, targetRect))
	                targetsToDelete.add(target);
	        }
	 
	        for (CCSprite target : targetsToDelete)
	        {
	            _targets.remove(target);
	            removeChild(target, true);
	        }
	 
	        if (targetsToDelete.size() > 0)
	            projectilesToDelete.add(projectile);
	    }
	 
	    for (CCSprite projectile : projectilesToDelete)
	    {
	        _projectiles.remove(projectile);
	        removeChild(projectile, true);
	    }
	}
	
	public static boolean intersects(CGRect a, CGRect b)
    {
    	return (a.origin.x >= (b.origin.x - a.size.width) && a.origin.x <= (b.origin.x - a.size.width) + (b.size.width + a.size.width)
    			&& a.origin.y >= (b.origin.y - a.size.height) && a.origin.y <= (b.origin.y - a.size.height) + (b.size.height + a.size.height));
    }

}
