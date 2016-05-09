package skyland.util;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;

import net.minecraftforge.classloading.FMLForgePlugin;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import skyland.core.Skyland;

public class Version
{
	private static String CURRENT;

	public static boolean DEV_DEBUG = false;

	public static void initVersion()
	{
		CURRENT = Strings.nullToEmpty(Skyland.metadata.version);

		ModContainer mod = SkyUtils.getModContainer();
		File file = mod == null ? null : mod.getSource();

		if (file != null && file.exists())
		{
			if (file.isFile())
			{
				String name = FilenameUtils.getBaseName(file.getName());

				if (StringUtils.endsWithIgnoreCase(name, "dev"))
				{
					DEV_DEBUG = true;
				}
			}
			else if (file.isDirectory())
			{
				DEV_DEBUG = true;
			}
		}
		else if (!FMLForgePlugin.RUNTIME_DEOBF)
		{
			DEV_DEBUG = true;
		}

		if (Skyland.metadata.version.endsWith("dev"))
		{
			DEV_DEBUG = true;
		}
		else if (DEV_DEBUG)
		{
			Skyland.metadata.version += "-dev";
		}
	}

	public static CheckResult getResult()
	{
		return ForgeVersion.getResult(SkyUtils.getModContainer());
	}

	public static Status getStatus()
	{
		return getResult().status;
	}

	public static String getCurrent()
	{
		return CURRENT;
	}

	public static ComparableVersion getLatest()
	{
		ComparableVersion ret = getResult().target;

		if (ret == null)
		{
			return new ComparableVersion(CURRENT);
		}

		return ret;
	}

	public static boolean isOutdated()
	{
		return getStatus() == Status.OUTDATED || getStatus() == Status.BETA_OUTDATED;
	}

	public static boolean isBeta()
	{
		return StringUtils.containsIgnoreCase(CURRENT, "beta");
	}

	public static boolean isAlpha()
	{
		return StringUtils.containsIgnoreCase(CURRENT, "alpha");
	}
}